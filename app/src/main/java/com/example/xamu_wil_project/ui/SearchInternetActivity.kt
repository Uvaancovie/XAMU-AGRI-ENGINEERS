package com.example.xamu_wil_project.ui

import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.example.xamu_wil_project.R
import java.net.URLEncoder

class SearchInternetActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_internet)
        searchView = findViewById(R.id.searchView); webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient(); webView.settings.javaScriptEnabled = true
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean { q?.let { search(it) }; return true }
            override fun onQueryTextChange(newText: String?) = true
        })
        searchView.isIconified = false; searchView.requestFocus()
    }
    private fun search(q: String) {
        val url = "https://www.google.com/search?q=${URLEncoder.encode(q, "UTF-8")}"
        webView.loadUrl(url)
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) { webView.goBack(); return true }
        return super.onKeyDown(keyCode, event)
    }
}
