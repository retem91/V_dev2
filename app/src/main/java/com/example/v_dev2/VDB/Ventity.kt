package com.example.v_dev2.VDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stockHistory")
data class stockHistory(
    val name: String,
    val code: String,
    var date: String,
    var history: String,
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(tableName = "vTable") // 네임이 유니크한 값이니까 네임으로 업데이트하도록
data class vProfile(
    val vName: String,
    val vCode : String,
    var vTheme: String,
    var property: String,
    val themeRank: Int,
    val vtime: String,
    val date: String
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(tableName = "rank")
data class rank(
    val stockName: String,
    val Value : String,
    var Theme : String,
    var rankType : String,
    @PrimaryKey
    var rank: Int
)

@Entity(tableName = "news") // 네임이 유니크한 값이니까 네임으로 업데이트하도록
data class News(
    val Title : String,
    val Publisher : String,
    var Time : String,
    var date : String,
    @PrimaryKey(autoGenerate = true)
    var id: Int
)