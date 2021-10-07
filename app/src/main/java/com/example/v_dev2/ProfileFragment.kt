package com.example.v_dev2

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.v_dev2.VDB.stockHistory
import com.example.v_dev2.databinding.FragmentProfileBinding
import com.example.v_dev2.databinding.ItemRecyclerBinding
import java.net.Socket
import kotlinx.coroutines.*


class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding
    lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity) mainActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        val viName = this.requireArguments().getString("viName" , "default")
        val viTime = this.requireArguments().getString("viTime" , "default")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = Socket("14.46.161.156" , 13955)
                client.outputStream.write("짤랑!".toByteArray())
                client.close()
            } catch (e: Exception) {}
        }
        with(binding) {
            dbClear.setOnClickListener {
                // 아마 여기에 뉴스보기 넣을듯
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val historyList = mainActivity.db.historyDao().getAll(viName)
            val customadapter = HistoryCustomAdapter(historyList)
            mainActivity.runOnUiThread {
                binding.recyclerView.adapter = customadapter
                binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity)
            }
        }
    }
}

class HistoryCustomAdapter(var listData: MutableList<stockHistory> ) : RecyclerView.Adapter<HistoryCustomAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false) // 고정
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) { // 위 아래로 스크롤할때마다 호출될거야
        //1. 사용할 데이터를 꺼내고
        val history = listData.get(position)
        //2. 홀더에 데이터를 전달
        holder.setHistory(history)
    }

    override fun getItemCount(): Int = listData.size

    class Holder(val binding: ItemRecyclerBinding) : RecyclerView.ViewHolder(binding.root) {
        // 받은 데이터를 화면에 출력한다
        fun setHistory(history: stockHistory) {
            with(binding) {
                textChat.text = history.history
                textSender.text = history.date
            }
        }
    }
}