
/*
 * Copyright 2016 Pankaj Joshi (pankaj.joshi95@gmail.com)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package hacks.pankaj.headsupbrowser;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.HashMap;

public class BrowserService extends Service {

    private static final int REQUEST_CODE = 555;
    private static final int NOTIFICATION_ID = 555;

    private boolean movable = false; /// boolean to check weather to move the browser when selected tab is dragged

    private WindowManager windowManager;
    private WindowManager.LayoutParams nonFocusableParams;
    private WindowManager.LayoutParams focusableParams;
    private LayoutInflater inflater;

    private FrameLayout root;
    private FrameLayout container;
    private LinearLayout tabsContainer;
    private ImageView browserHead;
    private ImageView settings;
    private ImageView selectedTab;
    private HashMap<View,BrowserTab> browserTabsMap;
    private FrameLayout browserContainer;

    private View resizeView;
    private View lastHeadView;

    public BrowserService() {

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        browserTabsMap = new HashMap<>();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        nonFocusableParams = generateLayoutParams(false);
        focusableParams = generateLayoutParams(true);
        root = new FrameLayout(this);

        inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        container= (FrameLayout) inflater.inflate(R.layout.heads_up_service, root,false);
        tabsContainer = (LinearLayout) container.findViewById(R.id.tabs_container);
        browserContainer = (FrameLayout) container.findViewById(R.id.browser_container);

        settings = (ImageView) inflater.inflate(R.layout.head_view,tabsContainer,false);
        settings.setImageResource(R.drawable.settings);
        tabsContainer.addView(settings);
        resizeView = inflater.inflate(R.layout.resize_view,browserContainer,false);
        resizeView.setOnTouchListener(new browserResizeListener());
        settings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                movable = true; // dragging the selected tab will move the browser
                browserContainer.removeAllViews();
                browserContainer.addView(resizeView);

                displaySelectedTabView(view,R.drawable.move);


            }
        });
        nonFocusableParams.x=getResources().getDisplayMetrics().widthPixels/2;
        nonFocusableParams.y=-getResources().getDisplayMetrics().heightPixels/2;
        browserHead = (ImageView) inflater.inflate(R.layout.head_view,root,false);
        browserHead.setOnTouchListener(new headTouchListener());
        selectedTab =(ImageView) inflater.inflate(R.layout.head_view,root,false);
        selectedTab.setOnTouchListener(new tabTouchListener());
        windowManager.addView(root, nonFocusableParams);
        root.addView(browserHead);
    }

    private WindowManager.LayoutParams generateLayoutParams(boolean focusable) {
        WindowManager.LayoutParams params;
        if(!focusable) {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }else {
            params = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT);
        }
        return params;
    }

    private void initializeTab(String url){
        View headView = inflater.inflate(R.layout.head_view, tabsContainer,false);
        BrowserTab browserTab = new BrowserTab(inflater,browserContainer,url);
        browserTabsMap.put(headView,browserTab);
        headView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayTab(view);

            }
        });
        tabsContainer.addView(headView);
        if(lastHeadView==null){
            lastHeadView=headView;
        }

    }

    public void displayTab(View view){
        movable =false; // dragging the selected tab will not move the browser
        BrowserTab tab=browserTabsMap.get(view);
        browserContainer.removeAllViews();
        lastHeadView = view;
        browserContainer.addView(tab.getView());
        displaySelectedTabView(lastHeadView,R.drawable.browser_head);
    }

    // display the browser_head for selected tab
    public void displaySelectedTabView(View view, int imageResource){
        selectedTab.setX(view.getX());
        selectedTab.setY(view.getY());
        selectedTab.setImageResource(imageResource);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url ="http://www.google.com";
        if(intent.getData()!=null) url = intent.getData().toString();
        initializeTab(url);
        startForeground(NOTIFICATION_ID,getNotification());

        return super.onStartCommand(intent, flags, startId);
    }

    private Notification getNotification(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.browser_head)
                .setContentTitle("Heads Up Browser")
                .setTicker("Heads Up Browser running")
                .setContentText(browserTabsMap.size()+" Tabs active. Touch to close")
                .setWhen(System.currentTimeMillis());
        Intent startIntent = new Intent(this,MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,REQUEST_CODE,startIntent,0);
        builder.setContentIntent(contentIntent);
        return builder.build();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(root !=null)
            windowManager.removeView(root);
    }

    private class browserResizeListener implements View.OnTouchListener{

        private int width;
        private int height;
        private float rawX;
        private float rawY;
        boolean  move = false;
        private int newWidth;
        private int newHeight;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    move =false;
                    width = browserContainer.getWidth();
                    height = browserContainer.getHeight();
                    rawX = motionEvent.getRawX();
                    rawY=motionEvent.getRawY();

                    return true;
                case MotionEvent.ACTION_UP:
                    if(!move){
                        displayTab(lastHeadView);
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if(Math.abs(motionEvent.getRawX()-rawX)>0||Math.abs(motionEvent.getRawY()-rawY)>0)move = true;
                    newWidth = width + (int)(motionEvent.getRawX()-rawX);
                    newHeight = height + (int)(motionEvent.getRawY()-rawY);
                    newWidth = (newWidth>100)?newWidth:100;
                    newHeight = (newHeight>200)?newHeight:200;
                    container.updateViewLayout(browserContainer,new FrameLayout.LayoutParams(newWidth,newHeight));
                    return true;

            }
            return false;
        }
    }

    private class headTouchListener implements View.OnTouchListener{
        private int X;
        private int Y;
        private float rawX;
        private float rawY;
        boolean  move = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    move =false;
                    X = nonFocusableParams.x;
                    Y = nonFocusableParams.y;
                    rawX = motionEvent.getRawX();
                    rawY = motionEvent.getRawY();
                    Log.d("headsU", "onTouch: " +X+" "+Y+" "+ rawX +" " +rawY);
                    return true;
                case MotionEvent.ACTION_UP:
                    if(!move) {
                        root.removeAllViews();
                        root.addView(container);
                        root.addView(selectedTab);
                        displayTab(lastHeadView);
                        windowManager.updateViewLayout(root, focusableParams);
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if(Math.abs(motionEvent.getRawX()-rawX)>0||Math.abs(motionEvent.getRawY()-rawY)>0)move = true;
                    nonFocusableParams.x = X + (int)(motionEvent.getRawX()-rawX);
                    nonFocusableParams.y = Y + (int)(motionEvent.getRawY()-rawY);
                    windowManager.updateViewLayout(root, nonFocusableParams);
                    return true;

            }
            return false;
        }
    }
    private class tabTouchListener implements View.OnTouchListener{
        private int X;
        private int Y;
        private float rawX;
        private float rawY;
        boolean  move = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    move =false;
                    X = focusableParams.x;
                    Y = focusableParams.y;
                    rawX = motionEvent.getRawX();
                    rawY = motionEvent.getRawY();
                    Log.d("headsU", "onTouch: " +X+" "+Y+" "+ rawX +" " +rawY);
                    return true;
                case MotionEvent.ACTION_UP:
                    if(!move) {
                        root.removeAllViews();
                        root.addView(browserHead);
                        windowManager.updateViewLayout(root, nonFocusableParams);
                    }

                    return true;
                case MotionEvent.ACTION_MOVE:
                    if(Math.abs(motionEvent.getRawX()-rawX)>0||Math.abs(motionEvent.getRawY()-rawY)>0)move = true;
                    if(movable){
                        focusableParams.x = X + (int)(motionEvent.getRawX()-rawX);
                        focusableParams.y = Y + (int)(motionEvent.getRawY()-rawY);
                        windowManager.updateViewLayout(root, focusableParams);
                    }
                    return true;

            }
            return false;
        }
    }
}
