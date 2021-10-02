package com.example.v_dev2.VDB

import androidx.room.Entity
import androidx.room.PrimaryKey

//@Entity(tableName = "Chat")
//data class Chat(var chat:String,var room:String,var sender:String, val profile:String, var timestamp: Long,  @PrimaryKey var id: Long)

@Entity(tableName = "vTable") //
data class vProfile(var vName:String,var vTheme:String,val property: String,val themeRank:Int, var vtime: String, @PrimaryKey var id: Int)