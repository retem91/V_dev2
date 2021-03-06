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
import com.example.v_dev2.VDB.rank
import com.example.v_dev2.VDB.stockHistory
import com.example.v_dev2.VDB.vProfile
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket
import kotlin.math.round

class ForeDataReciever : Service() {

    val ChannelID = "DataReciever"
    val ChannelName = "DataRecieverService"
    val notificationTitle = "Recieving Realtime Data.."
    val NOTI_ID = 106
    lateinit var session : Socket
    var outputStream : OutputStream? = null
    lateinit var db : VDB

    fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(ChannelID,ChannelName,NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this,ChannelID)
            .setContentTitle(notificationTitle)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        startForeground(NOTI_ID,notification)
        db = VDB.getInstance(this)!!
        keepConnect()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // run when application start, resume
        val req = intent?.getStringExtra("req")
        if (req != null)
            requestData(req)
        Log.w("request","$req")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun keepConnect() {
        CoroutineScope(Dispatchers.IO).launch {
            while(true){
                try {
                    session = Socket("14.46.161.156", 8643)
                    session.keepAlive = true
                    var reader = BufferedReader(InputStreamReader(session.getInputStream()))
                    outputStream = session.getOutputStream()
                    outputStream!!.write("Hi".toByteArray())
                    Log.e("HI Sent", "1")
                    outputStream!!.flush()
                    while(true){
                        val data = reader.readLine()
                        val dataType = data.split("|")[0]
                        val parsedData = data.split("|")[1].split(":")
                        Log.e("Data Recieved", data)
                        when(dataType){
                            "vion" -> {vionRecieved(parsedData)}
                            "history" -> {historyRecieved(parsedData)}
                            "news" -> {newsRecieved(parsedData)}
                            "????????????rank" -> {runningStocksRecieved(dataType,parsedData)}
                            "?????????rank" -> {runningStocksRecieved(dataType,parsedData)}
                            "???????????????rank" -> {runningStocksRecieved(dataType,parsedData)}
                            else -> Log.e("Data Recieved", "Unknown Type : $dataType")
                        }
                    }
                }catch(e: Exception){
                    Log.e("Socket",e.stackTraceToString())
                    session.close()
                    delay(100)
                }
            }
        }
    }

    private fun runningStocksRecieved(rankType : String, data: List<String>) {
        try{
            val rankList = mutableListOf<rank>()
            var rank = when(rankType) {
                "?????????rank" -> 1
                "????????????rank" -> 101
                "???????????????rank" -> 201
                else -> 1
            }

            for (d in data){
                val parse = d.split("-")
                val stockName = d.split("-")[0]
                val value : String
                if (rankType == "?????????rank")
                    value = d.split("-")[1]
                else
                    value = (round(d.split("-")[1].toDouble() / 1000000) / 100).toString() + "???"
                // ????????? ?????? ???????????????, ???????????? ??????????????? ???????????? ???????????? ???????????? \n?????? ???????????? ?????????, ????????????????????? ????????? ???????????? ??????????????? ?????? ????????????????????? ?????? ?????????
                // ????????????rank, ??????????????????-1195334800,?????????-672372000,, ?????????rank,
                rankList.add(rank(stockName,value,"????????????",rankType, rank))
                rank ++
            }

            db.rankDao().insertAll(rankList)
            Log.w("Rank DB","??????")
        }catch (e:Exception){
            Log.e("runningStocksRecieved ??????", e.stackTraceToString())
        }
    }

    private fun historyRecieved(data: List<String>) {
        try {
            val name = data[0]
            val code = data[1]
            val date = data[2]
            val history = data[3]
            CoroutineScope(Dispatchers.IO).launch {
                db.historyDao().insertAll(stockHistory(name,code, date, history))
                Log.w("History DB","??????")
            }
        }catch (e:Exception){
            Log.e("historyRecieved",e.stackTraceToString())
        }
    }

    private fun newsRecieved(data: List<String>) {
        try {
            val title = data[0]
            val publisher = data[1]
            val time = data[2]
            val code = data[3]
            val tjFlag = data[4]
        } catch (e:java.lang.Exception){
            Log.e("newsRecieved", e.stackTraceToString())
        }
    }

    private fun vionRecieved(data: List<String>){
        try {
            val name = data[0]
            val code = data[1]
            val theme = data[2]
            val property = data[3]
            val rank = data[4].toInt()
            val time = data[5]
            val date = data[6]
            CoroutineScope(Dispatchers.IO).launch {
                db.vDao().insertAll(vProfile(name, code, theme, property, rank, time, date))
                Log.e("vion DB", "??????")
            }
        }catch(e:Exception){
            Log.e("vionRecieved", e.stackTraceToString())
        }
    }

    private fun requestData(request: String) {
        Log.w("REQ ??????", request)
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 1..10) {
                try {
                    if(outputStream != null) {
                        outputStream!!.write(request.toByteArray())
                        outputStream!!.flush()
                        Log.e("REQ Sent", request)
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