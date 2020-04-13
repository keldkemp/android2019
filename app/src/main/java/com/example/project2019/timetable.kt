package com.example.project2019

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_timetable.*
import java.net.URLEncoder
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_timetable.view.*
import java.util.*


class timetable : Fragment() {

    companion object {
        private var USERNAME = ""
        private var PASSWORD = ""
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

        //Разрешаем zoom, JS
        web.settings.builtInZoomControls = true
        web.settings.setSupportZoom(true)
        web.settings.displayZoomControls = false
        web.settings.javaScriptEnabled = true

        //При первом посещении данного фрагмента
        if (USERNAME == "" || PASSWORD == "") {
            val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
            val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
            raw.moveToFirst()

            USERNAME = raw.getString(2)
            PASSWORD = raw.getString(3)

            val url = "https://student.psu.ru/pls/stu_cus_et/stu.login"
            val postdata = "p_username=" + URLEncoder.encode(USERNAME, "windows-1251") + "&p_password=" + URLEncoder.encode(
                PASSWORD, "UTF-8")
            //{'p_username': username.encode('windows-1251'), 'p_password': password}

            web.postUrl(url, postdata.toByteArray())

            Thread.sleep(1000)
        }

        web.loadUrl("https://student.psu.ru/pls/stu_cus_et/stu.timetable")

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
