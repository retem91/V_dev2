package com.example.v_dev2

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*

import com.example.v_dev2.VDB.VDB
import com.example.v_dev2.VDB.rank
import com.example.v_dev2.databinding.FragmentRankBinding
import com.example.v_dev2.databinding.RankitemRecyclerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class RankFragment : Fragment() {
    lateinit var binding: FragmentRankBinding
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
        binding = FragmentRankBinding.inflate(inflater, container, false)
        mainActivity.serviceStart("subcribe")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        db = VDB.getInstance(mainActivity)!!
        val displayMetrics = requireContext().resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels


        db.rankDao().rankLiveSelect("거래대금rank").observe(viewLifecycleOwner , androidx.lifecycle.Observer {
            // 어댑터 따로 빼는건 나중에 성능문제 생기면 하자 귀찮으니까.
            var rank1value : Float = 0.toFloat()
            rank1value = it[0].Value.replace("억","").toFloat()
            val customadapter = RankCustomAdapter(it, rank1value, dpWidth)
            binding.mapitemRecycler.adapter = customadapter

            // 4. 레이아웃 매니저 설정
            binding.mapitemRecycler.layoutManager = LinearLayoutManager(mainActivity)
            registerForContextMenu(binding.mapitemRecycler)
        })
    }
}

class RankCustomAdapter(val listData: MutableList<rank>, val rank1value: Float, val dpWidth: Int) : RecyclerView.Adapter<RankCustomAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = RankitemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false) // 고정
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) { // 위 아래로 스크롤할때마다 호출될거야
        //1. 사용할 데이터를 꺼내고
        val rankData = listData.get(position)
        //2. 홀더에 데이터를 전달
        val relativeLength = rankData.Value.replace("억","").toFloat() / rank1value
        val rankWidth = relativeLength * dpWidth.toFloat()

        Log.w("AA","${rankData.Value} $rank1value $relativeLength $dpWidth $rankWidth ${rankData.stockName}")
        holder.setMemo(rankData,position, rankWidth)
    }

    override fun getItemCount(): Int = listData.size

    class Holder(val binding: RankitemRecyclerBinding) : RecyclerView.ViewHolder(binding.root) {
        // 받은 데이터를 화면에 출력한다
        fun setMemo(profile: rank, position: Int, rankWidth: Float) {
            with(binding) {
                themeRank.text = "${position + 1} 위"
                stockName.text = profile.stockName
                first.text = profile.Value
                first.width = rankWidth.roundToInt()
            }
        }
    }
}