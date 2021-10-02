package com.example.v_dev2.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.v_dev2.MainActivity
import com.example.v_dev2.R
import com.example.v_dev2.VDB.VDB
import com.example.v_dev2.VDB.vProfile
import com.example.v_dev2.databinding.FragmentHomeBinding
import com.example.v_dev2.databinding.VitemRecyclerBinding
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    lateinit var mainActivity: MainActivity
    lateinit var db: VDB



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity) mainActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = VDB.getInstance(mainActivity)!!

        db.vDao().chatLiveSelect().observe(mainActivity , androidx.lifecycle.Observer {
            var profileList: MutableList<vProfile> = mutableListOf()
            for (profile in it) {
                profileList.add(profile)
            }
            val customadapter = ProfileListCustomAdapter(profileList , mainActivity , db)
            binding.Vrecycler.adapter = customadapter
            // 4. 레이아웃 매니저 설정
            binding.Vrecycler.layoutManager = LinearLayoutManager(mainActivity)
            registerForContextMenu(binding.Vrecycler)

            //            (binding.recyclerView.layoutManager as LinearLayoutManager).reverseLayout = true
            //            binding.recyclerView.scrollToPosition((binding.recyclerView.adapter as ProfileListCustomAdapter).getItemCount() - 1)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


class ProfileListCustomAdapter(val listData : MutableList<vProfile>, val mainActivity: MainActivity, val db: VDB) : RecyclerView.Adapter<ProfileListCustomAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = VitemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false) // 고정
        return Holder(binding, mainActivity, db)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) { // 위 아래로 스크롤할때마다 호출될거야
        //1. 사용할 데이터를 꺼내고
        val memo = listData.get(position)
        //2. 홀더에 데이터를 전달
        holder.setMemo(memo)
    }

    override fun getItemCount(): Int = listData.size

    class Holder(val binding: VitemRecyclerBinding, val mainActivity: MainActivity, val db: VDB) : RecyclerView.ViewHolder(binding.root) {
        var currentChat: vProfile? = null
        val pattern = "HH:mm"
        val formatter = SimpleDateFormat(pattern)

        // 클릭 처리는 init에서만 한다, Holder는 창에 보일만큼(여기선 17개) 만들어진다. 17개의 홀더가 있고, 클릭리스너도 17개가 달리는거, 그리고 커런트 메모 또한 그 홀더에
        //박힌 메모값이 박히는거라 홀더 전체가 삭제되고 리사이클될때까지 변하지 않는다. 난 보이는 화면 전체가 홀더인줄 알았는데, 각 라인하나씩이 홀더였다.
        init {
            with(binding) {
                root.setOnClickListener {
                    if (smallview.getVisibility() == View.GONE) {
                        smallview.setVisibility(View.VISIBLE)
                        bigview.visibility = View.GONE
                    } else {
                        smallview.setVisibility(View.GONE)
                        bigview.visibility = View.VISIBLE
                    }
                }
            }
            // will be written later..
//            binding.root.setOnLongClickListener{
//                val pop= PopupMenu(binding.root.context,it, Gravity.END)
//                pop.inflate(R.menu.profilemenu)
//                pop.setOnMenuItemClickListener {item->
//                    when(item.itemId)
//                    {
//                        R.id.delete->{
//                            val uid = VApplication.prefs.getString(currentChat!!.profileName,"").split(":")[4]
//                            thread(start = true) {
//                                db.profileDao().deleteItemById(uid.toInt())
//                                VApplication.prefs.delPref(currentChat!!.profileName)
//                                Log.w("프로파일 삭제 결과",VApplication.prefs.getPreflist().toString())
//                            }
//                        }
//                        R.id.change->{
//                            mainActivity.goSetting(currentChat!!.profileName)
//                        }
//                        R.id.clear->{
//                            val uid = VApplication.prefs.getString(currentChat!!.profileName,"").split(":")[4]
//                            thread(start = true) {
//                                db.chatDao().deleteItemByProfileName(currentChat!!.profileName)
////                                db.profileDao().updateProfile("","","",0,uid.toInt())
//                                db.profileDao().clearProfile(uid.toInt())
//                            }
//                        }
//                        R.id.cancel->{ }
//                    }
//                    true
//                }
//                pop.show()
//                true
//            }
        }

        // 받은 데이터를 화면에 출력한다
        fun setMemo(profile: vProfile) {
            currentChat = profile
            with(binding) {

                Title.text = profile.vName
                vproperty.text = profile.themeRank.toString()
                postTime.text = profile.vtime
                smallview.text = profile.vtime
//                senderPic.setImageResource()
            }
        }
    }
}