package com.example.project2019

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.fragment_timetable.view.*


class timetable : Fragment() {

    companion object {
        private var USERNAME = ""
        private var SESSION_ID = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)

    }

    //Открывает ВебВью в приложении, Расписание пользователя
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val View = inflater.inflate(R.layout.fragment_timetable, container, false)

        val web: WebView = View.web_timetable

        val url = "https://student.psu.ru/pls/stu_cus_et/stu.timetable"

        //Разрешаем zoom, JS
        web.settings.builtInZoomControls = true
        web.settings.setSupportZoom(true)
        web.settings.displayZoomControls = false
        web.settings.javaScriptEnabled = true

        //При первом посещении данного фрагмента
        if (USERNAME == "" || SESSION_ID == "") {

            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            val settings = activity!!.baseContext.getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
            val session_id = settings.getString("session_id", null) ?: ""


            USERNAME = Main2Activity.USERNAME
            SESSION_ID = session_id

            //val postdata = "p_username=" + URLEncoder.encode(USERNAME, "windows-1251") + "&p_password=" + URLEncoder.encode(
            //    SESSION_ID, "UTF-8")
            //{'p_username': username.encode('windows-1251'), 'p_password': password}

            cookieManager.setCookie(url, "session_id=$session_id")

            //web.postUrl(url, postdata.toByteArray())

            //Thread.sleep(1000)
        }

        web.loadUrl(url)
        Log.d("Web", "OK")

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                web.loadUrl("javascript:(function() { " +
                        "var head = document.getElementsByClassName('span3')[0].remove();"
                        + "var item = document.getElementsByClassName('estimate_tt')[0].remove();" + "})()")
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                web.loadUrl("javascript:(function() { " +
                        "var head = document.getElementsByClassName('span3')[0].remove();"
                        + "var item = document.getElementsByClassName('estimate_tt')[0].remove();" + "})()")
            }
        }

        return View
    }



}
