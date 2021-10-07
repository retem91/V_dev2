package com.example.v_dev2.VDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stockHistory")
data class stockHistory(
    val name: String,
    var date: String,
    var history: String,
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(tableName = "vTable") // 네임이 유니크한 값이니까 네임으로 업데이트하도록
data class vProfile(
    val vName: String,
    var vTheme: String,
    var property: String,
    val themeRank: Int,
    val vtime: String,
    val date: String
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}