package com.example.project2019

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.ColorFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.fragment_menu.view.*
import okhttp3.*
import java.io.IOException


class Menu : Fragment() {

    private var name = ""
    private val client = OkHttpClient()

    val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            Toast.makeText(Main2Activity.CONTEXT, message.obj.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val View = inflater.inflate(R.layout.fragment_menu, container, false)
        val TextView = View.menu_student_name
        val Button = View.btm_change_password

        if (name == "") {
            val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

            val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
            raw.moveToFirst()

            name = raw.getString(1)

            db.close()
        }

        TextView.text = String.format(View.context.getString(R.string.student, name))
        TextView.setTextColor(Color.BLACK)
        TextView.setBackgroundColor(Color.WHITE)
        TextView.setTextSize(20.toFloat())

        Button.setOnClickListener(object: android.view.View.OnClickListener {
            override fun onClick(view: View) {
                changepassword(view)
            }
        })

        return View
    }

    //Смена пароля на сайте (вызывается в методе changepassword)
    private fun changepassworddb(username: String, name: String, old_password: String, new_password: String, context: Context, btm: Button) {

        val formBody = FormBody.Builder()
            .add("username", username)
            .add("name", name)
            .add("old_password", old_password)
            .add("new_password", new_password)
            .build()
        val request = Request.Builder()
            .url("https://crmproject2019.herokuapp.com/api/etis/updatepassword")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.code() == 200) {
                    val settings = activity!!.baseContext.getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString("password", new_password)
                    editor.apply()

                    btm.isClickable = true

                    val msg = mHandler.obtainMessage(1, "Пароль изменен")
                    msg.sendToTarget()
                }
                else if (response.code() == 403){

                    val msg = mHandler.obtainMessage(1, "Введен неверно старый пароль")
                    msg.sendToTarget()
                    btm.isClickable = true
                }
                else if (response.code() == 402){

                    val msg = mHandler.obtainMessage(1, "В данный момент пароль изменить нельзя")
                    msg.sendToTarget()
                    btm.isClickable = true
                }
                else if (response.code() == 401) {

                    val msg = mHandler.obtainMessage(1, "Вы не заполнили одно из полей")
                    msg.sendToTarget()
                    btm.isClickable = true
                }
                else {
                    val msg = mHandler.obtainMessage(1, "Неизвестная ошибка")
                    msg.sendToTarget()
                    btm.isClickable = true
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("ChangePassword", "ChangePassword - Fatal-Error, Service is down")
                btm.isClickable = true
                val msg = mHandler.obtainMessage(1, "Сервис недоступен, попробуйте позже")
                msg.sendToTarget()
            }
        })
    }

    //Срабатывает по кнопке, смена пароля
    private fun changepassword(view: View) {

        val btm = view.btm_change_password
        val username: String = Main2Activity.USERNAME
        var old_password: String
        var new_password: String
        val name: String

        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
        raw.moveToFirst()

        name = raw.getString(1)

        val layout = LayoutInflater.from(view.context)
        val alert_dialog_view = layout.inflate(R.layout.alert_dialog_update_password, null)

        val mDialogBuilder = AlertDialog.Builder(view.context)

        mDialogBuilder.setView(alert_dialog_view)

        val old_password_view: EditText = alert_dialog_view.findViewById(R.id.old_password)
        val new_password_view: EditText = alert_dialog_view.findViewById(R.id.new_password)

        mDialogBuilder
            .setCancelable(false)
            .setPositiveButton("Сменить пароль"
            ) { dialog, _ ->
                //Вызываем функцию по смене пароля
                old_password = old_password_view.text.toString()
                new_password = new_password_view.text.toString()

                if (old_password.isEmpty() || new_password.isEmpty()){
                    val msg = mHandler.obtainMessage(1, "Заполните оба поля")
                    msg.sendToTarget()
                } else {

                    btm.isClickable = false

                    changepassworddb(username, name, old_password, new_password, view.context, btm)

                    dialog.cancel()
                }
            }
            .setNegativeButton("Отмена",
                object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id:Int) {
                        dialog.cancel()
                    }
                })
        //Создаем AlertDialog:
        val alertDialog = mDialogBuilder.create()

        //и отображаем его:
        alertDialog.show()

    }

}
