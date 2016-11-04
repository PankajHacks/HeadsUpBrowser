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


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Pankaj on 01-11-2016.
 */

public class BrowserTab {
    private View browserView;
    private WebView webView;
    private boolean selected=false;

    private int imageResource = R.drawable.browserHead;
    //TODO: download website favicon for image resource;

    public BrowserTab(LayoutInflater inflater, ViewGroup parent, String url){
        browserView = inflater.inflate(R.layout.browser_tab,parent,false);
        webView = (WebView) browserView.findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        try {
            webView.loadUrl(url);
        }catch (Exception e){
            webView.loadUrl("https://www.google.com");
        }
    }

    public View getView(){
        return browserView;
    }
    public boolean isSelected(){
        return selected;
    }
    public void setSelected(boolean val){
        selected=val;
    }
}
