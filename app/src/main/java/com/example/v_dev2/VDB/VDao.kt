package com.example.v_dev2.VDB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.sql.Timestamp

//@Dao
//interface VDao {
//    @Query("SELECT * FROM Chat WHERE v = :vName")
//    fun chatLiveSelect(vName: String) : LiveData<MutableList<Chat>>
//
//    @Query("SELECT * FROM Chat")
//    fun getAll(): List<Chat>
//
//    @Query("SELECT COUNT(*) FROM Chat")
//    fun getcount(): Int
//
//    @Query("SELECT * FROM Chat WHERE id IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<Chat>
//
////        @Query("SELECT * FROM URL WHERE first_name LIKE :first AND " +
////                "last_name LIKE :last LIMIT 1")
////        fun findByName(first: String, last: String): URL
//
//    @Insert
//    fun insertAll(vararg users: Chat)
//
////    fun insertIfNotDup(chat:String, room:String, sender:String, v:String, timestamp: Long){
////        if (dupCheck(chat, room, sender, v, timestamp).size == 0)
////            insertAll(Chat(chat, room, sender, v, timestamp))
////    }
////
////    @Query("SELECT * FROM Chat WHERE chat = :chat AND room = :room AND sender = :sender AND v = :v AND timestamp = :timestamp")
////    fun dupCheck(chat:String, room:String, sender:String, v:String, timestamp: Long): List<Chat>
//
//    @Delete
//    fun delete(url: Chat)
//
//    @Query("DELETE FROM Chat")
//    fun deleteall()
//
//    @Query("DELETE FROM Chat WHERE v = :vName")
//    fun deleteItemByvName(vName: String)
//}


//data class vProfile(var vName:String,var vTheme:MutableList<String>,  val themeRank:Int, var vtime: String, @PrimaryKey var id: Int)
@Dao
interface vDao {
    @Query("SELECT * FROM vTable order by vtime DESC") // use LIKE to get todays data only
    fun chatLiveSelect() : LiveData<MutableList<vProfile>>

    @Query("SELECT * FROM vTable")
    fun getAll(): List<vProfile>

    @Query("UPDATE vTable SET vName = :vName, vTheme = :vTheme, themeRank = :themeRank, vtime = :vtime WHERE id = :id")
    fun updatev(vName:String, vTheme:String, themeRank:Int, vtime: String, id: Int)

    @Query("SELECT COUNT(*) FROM vTable")
    fun getcount(): Int

    @Query("SELECT * FROM vTable WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<vProfile>

    @Insert
    fun insertAll(vararg vs: vProfile)

    @Delete
    fun delete(v: vProfile)

    @Query("DELETE FROM vTable WHERE id = :id")
    fun deleteItemById(id: Int)

    @Query("DELETE FROM vTable")
    fun deleteall()
}
