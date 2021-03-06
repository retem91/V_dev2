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
import com.example.v_dev2.databinding.FragmentMainBinding
import com.example.v_dev2.databinding.ProfileRecyclerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket


class MainFragment : Fragment() {
    lateinit var binding: FragmentMainBinding
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
        binding = FragmentMainBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
//        binding.dbClear.setOnClickListener{
//            CoroutineScope(Dispatchers.IO).launch {
//                db.vDao().deleteall()
//            }
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        db = VDB.getInstance(mainActivity)!!

        //여기에 DB 옵저브 하나 달아서 프로파일DB 만들고 연결하면 순서가 바뀔까?
        // 아니면 리니어 레이아웃에다가 달면... 프로파일에 새로운 데이터가 들어온걸 시그널 처리할 수 있나?
        // 라이브 데이터 형식으로 박는게 가장 이상적일거같은데... 밑에 저놈도 결국 라이브데이터라서 된거 아닌가 저걸 라이브데이터를 그 sort로 최근 메시지 도착 시간으로 정렬하면
        // 항상 최신 데이터가 왼쪽으로 가게 할수있을거같고, 온클릭 리스너 달기도 좋을거같은데?
        db.vDao().chatLiveSelect().observe(viewLifecycleOwner , androidx.lifecycle.Observer {
            var profileList: MutableList<vProfile> = mutableListOf()
            for (profile in it) {
                profileList.add(0,profile)
            }

            Log.w("profile list",profileList.toString())
            // 어댑터 따로 빼는건 나중에 성능문제 생기면 하자 귀찮으니까.
            val customadapter = ProfileListCustomAdapter(profileList , mainActivity, db)
            binding.recyclerView.adapter = customadapter

            // 4. 레이아웃 매니저 설정
            binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity)
            registerForContextMenu(binding.recyclerView)
            (binding.recyclerView.adapter as NewsListCustomAdapter).notifyDataSetChanged()

//            (binding.recyclerView.layoutManager as LinearLayoutManager).reverseLayout = true
//            binding.recyclerView.scrollToPosition((binding.recyclerView.adapter as ProfileListCustomAdapter).getItemCount() - 1)
        })
    }
}

class ProfileListCustomAdapter(val listData : MutableList<vProfile>, val mainActivity: MainActivity,val db: VDB) : RecyclerView.Adapter<ProfileListCustomAdapter.Holder>() {
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