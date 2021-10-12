package com.example.v_dev2

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*

import com.example.v_dev2.VDB.VDB
import com.example.v_dev2.VDB.stockHistory
import com.example.v_dev2.VDB.vProfile
import com.example.v_dev2.databinding.FragmentNewsBinding
import com.example.v_dev2.databinding.ProfileRecyclerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket


class NewsFragment : Fragment() {
    lateinit var binding: FragmentNewsBinding
    lateinit var mainActivity: MainActivity
    lateinit var db: VDB

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 메인액티비티의 인스턴스 사용할수 있게 해주는거
        if(context is MainActivity) mainActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        db = VDB.getInstance(mainActivity)!!

        db.vDao().chatLiveSelect().observe(viewLifecycleOwner , androidx.lifecycle.Observer {
            var profileList: MutableList<vProfile> = mutableListOf()
            for (profile in it) {
                profileList.add(0,profile)
            }
            val customadapter = NewsListCustomAdapter(profileList , mainActivity, db)
            binding.recyclerView.adapter = customadapter

            binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity)
            registerForContextMenu(binding.recyclerView)
        })
    }
}

class NewsListCustomAdapter(val listData : MutableList<vProfile>, val mainActivity: MainActivity, val db: VDB) : RecyclerView.Adapter<NewsListCustomAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ProfileRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false) // 고정
        return Holder(binding, mainActivity,db)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) { // 위 아래로 스크롤할때마다 호출될거야
        //1. 사용할 데이터를 꺼내고
        val memo = listData.get(position)
        //2. 홀더에 데이터를 전달
        holder.setMemo(memo)
    }

    override fun getItemCount(): Int = listData.size

    class Holder(val binding: ProfileRecyclerBinding, val mainActivity: MainActivity,val db: VDB) : RecyclerView.ViewHolder(binding.root) {
        var currentChat: vProfile? = null

        // 클릭 처리는 init에서만 한다, Holder는 창에 보일만큼(여기선 17개) 만들어진다. 17개의 홀더가 있고, 클릭리스너도 17개가 달리는거, 그리고 커런트 메모 또한 그 홀더에
        //박힌 메모값이 박히는거라 홀더 전체가 삭제되고 리사이클될때까지 변하지 않는다. 난 보이는 화면 전체가 홀더인줄 알았는데, 각 라인하나씩이 홀더였다.
        init {
            binding.root.setOnClickListener{
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val oldHistorys = db.historyDao().getAll(currentChat!!.vCode)
                        val client = Socket("14.46.161.156" , 8643)
                        client.outputStream.write("reqHistory|${currentChat!!.vCode}".toByteArray())
                        val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                        while (true) {
                            try {
                                val data = reader.readLine()
                                val dataType = data.split("|")[0]
                                val parsedData = data.split("|")[1].split(":")
                                Log.w("Data Recieved", parsedData.toString())
                                if (dataType == "history") {
                                    val name = parsedData[0]
                                    val code = parsedData[1]
                                    val date = parsedData[2]
                                    val history = parsedData[3]
                                    var isNew = true
                                    for (oldHistory in oldHistorys) {
                                        if (history == oldHistory.history)
                                            isNew = false
                                    }
                                    if (isNew)
                                        db.historyDao()
                                            .insertAll(stockHistory(name, code, date, history))
                                }
                            }catch (e:Exception){}
                        }
                        client.close()
                    }catch (e:Exception){
                        Log.e("historyRecieved",e.stackTraceToString())
                    }
                }
                val handler = Handler()
                handler.postDelayed(Runnable {
                    mainActivity.goViProfile(currentChat!!.vCode, currentChat!!.vtime.toString())
                    //write the code here which you want to run after 500 milliseconds
                } , 170)
            }
        }

        // 받은 데이터를 화면에 출력한다
        fun setMemo(profile: vProfile) {
            currentChat = profile
            with(binding) {
                profileName.text = profile.vName
                textChat.text = profile.property
                viTheme.text = profile.vTheme
                viTime.text = "${profile.vtime.toString().substring(0,2)}시 ${profile.vtime.toString().substring(2,4)}분 ${profile.vtime.toString().substring(4,6)}초"
            }
        }
    }
}