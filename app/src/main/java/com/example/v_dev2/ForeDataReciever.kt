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
                            "거래대금rank" -> {runningStocksRecieved(dataType,parsedData)}
                            "상승률rank" -> {runningStocksRecieved(dataType,parsedData)}
                            "시장가매수rank" -> {runningStocksRecieved(dataType,parsedData)}
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
                "상승률rank" -> 1
                "거래대금rank" -> 101
                "시장가매수rank" -> 201
                else -> 1
            }

            for (d in data){
                val parse = d.split("-")
                val stockName = d.split("-")[0]
                val value : String
                if (rankType == "상승률rank")
                    value = d.split("-")[1]
                else
                    value = (round(d.split("-")[1].toDouble() / 1000000) / 100).toString() + "억"
                // 여기서 자꾸 에러나는게, 데이터가 비어있을때 때문이라 깔끔하게 랭크별로 \n줄로 구분해서 보내고, 비어있을경우도 어떻게 처리할지 백엔드에서 할지 프론트엔드에서 할지 정해라
                // 거래대금rank, 피에이치에이-1195334800,큐라클-672372000,, 상승률rank,
                rankList.add(rank(stockName,value,"개별이슈",rankType, rank))
                rank ++
            }

            db.rankDao().insertAll(rankList)
            Log.w("Rank DB","성공")
        }catch (e:Exception){
            Log.e("runningStocksRecieved 에러", e.stackTraceToString())
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
                Log.w("History DB","성공")
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
                Log.e("vion DB", "성공")
            }
        }catch(e:Exception){
            Log.e("vionRecieved", e.stackTraceToString())
        }
    }

    private fun requestData(request: String) {
        Log.w("REQ 시작", request)
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