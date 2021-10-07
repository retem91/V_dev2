package com.example.v_dev2

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.v_dev2.VDB.VDB
import com.example.v_dev2.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var db: VDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding = ActivityMainBinding.inflate(layoutInflater)
        db = VDB.getInstance(this)!!
        setContentView(binding.root)

        val isNotificationAllowed = NotificationManagerCompat.from(this).areNotificationsEnabled()
        if (!isNotificationAllowed) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE , packageName)
            startActivity(intent)
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = Socket("14.46.161.156" , 13955)
                client.outputStream.write("짤랑!".toByteArray())
                client.close()
            }catch(e: Exception){}
        }
        setFragment()
        // 노티에서 특정 VI 알림을 눌렀을때 동작할 코드
        if (intent != null) {
            val viName = intent.getStringExtra("ViNoticlickedName")
            val viTime = intent.getStringExtra("ViNoticlickedTime")
            if (viName != null && viTime != null){
                Log.w("NotiClicked From Service", "$viName $viTime" )
                goViProfile(viName, viTime)
            }
        }
        serviceStart()
    }

    fun goViProfile(viName: String, viTime: String) {
        val bundle = Bundle()
        bundle.putString("viName", viName)
        bundle.putString("viTime", viTime)
        //1. 사용할 프래그먼트 생성
        val chatFragment = ProfileFragment()
        chatFragment.arguments = bundle
        //2. 트랜젝션 설정
        val transaction = supportFragmentManager.beginTransaction()
        //3. 트랜젝션을 통해 프래그먼트 삽입
        transaction.setCustomAnimations(R.anim.slide_in,R.anim.fade_out,R.anim.fade_in,R.anim.slide_out)
        transaction.replace(R.id.frameLayout1,chatFragment)

        transaction.addToBackStack("settings")
//        frameLayout.removeAllViews()
        transaction.commit()
    }

    fun setFragment(){
        //1. 사용할 프래그먼트 생성
        val mainFragment = MainFragment()
        //2. 트랜젝션 설정
        val transaction = supportFragmentManager.beginTransaction()
        //3. 트랜젝션을 통해 프래그먼트 삽입
        transaction.setCustomAnimations(R.anim.fade_in,R.anim.slide_out,R.anim.slide_in,R.anim.fade_out)
        transaction.replace(R.id.frameLayout1,mainFragment)
//        frameLayout.removeAllViews()
        transaction.commit()
    }

    fun serviceStart(){
        val intent = Intent(this,ForeDataReciever::class.java)
        ContextCompat.startForegroundService(this,intent)
    }

    fun serviceStop(){
        val intent = Intent(this,ForeDataReciever::class.java)
        stopService(intent)
    }
}