package com.example.v_dev2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.v_dev2.VDB.VDB
import com.example.v_dev2.VDB.vProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

class ForeDataReciever : Service() {

    val ChannelID = "DataReciever"
    val ChannelName = "DataRecieverService"
    val notificationTitle = "Recieving Realtime Data.."
    val NOTI_ID = 106
    lateinit var session : Socket
    var outputStream : OutputStream? = null
    lateinit var db : VDB
//hey!
    fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(ChannelID,ChannelName,NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }


    override fun onCreate() {
        super.onCreate()
        keepConnect()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // run when application start, resume
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this,ChannelID)
            .setContentTitle(notificationTitle)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        startForeground(NOTI_ID,notification)
        db = VDB.getInstance(this)!!
        val order = intent!!.getStringExtra("order")
        if (order != null && order != "")
            requestData(order)
        Log.w("service","start")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun keepConnect() {
        CoroutineScope(Dispatchers.IO).launch {
            while(true){
                try {
                    session = Socket("14.46.161.156", 8643)
                    val reader = BufferedReader(InputStreamReader(session.getInputStream()))
                    outputStream = session.getOutputStream()
                    outputStream!!.write("HI".toByteArray())
                    Log.e("HI Sent", "1")
                    outputStream!!.flush()
                    while(true){
                        val data = reader.readLine()
                        Log.e("Data Recieved", data)
                        val dataType = data.split("|")[0]
                        Log.e("Data Recieved", dataType)
                        Log.e("Data Recieved", data)
                        val parsedData = data.split("|")[1].split(":")
                        Log.e("Data Recieved", parsedData.toString())
                        when(dataType){
                            "vion" -> {vionRecieved(parsedData)}
                            "news" -> {newsRecieved(parsedData)}
                            else -> Log.e("Data Recieved", "Unknown Type : $dataType")
                        }
                    }
                }catch(e: Exception){
                    Log.e("Socket",e.stackTraceToString())
                    delay(3000)
                }
            }
        }
    }

    private fun newsRecieved(data: List<String>) {
        Log.w("news", "arrived")
        val title = data[0]
        val publisher = data[1]
        val time = data[2]
        val code = data[3]
        val tjFlag = false
    }

    private fun vionRecieved(data: List<String>){
        Log.w("vion", "arrived")
        val name = data[0]
        val theme = data[1]
        val property = data[2]
        val rank = data[3].toInt()
        val time = data[4].toInt()
//        db.vDao().insertAll(vProfile(name,theme,property,rank,time))
    }

    private fun requestData(request: String) {
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 1..10) {
                try {
                    if(outputStream != null) {
                        Log.e("Data Sent", "")
                        outputStream!!.write(request.toByteArray())
                        outputStream!!.flush()
                        break
                    }
                    delay(1000)
                }catch(e: Exception){
                    Log.e("Sent ERROR",e.stackTraceToString())
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }
}