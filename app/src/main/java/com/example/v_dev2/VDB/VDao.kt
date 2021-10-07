package com.example.v_dev2.VDB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.sql.Timestamp

@Dao
interface historyDao {
    @Query("SELECT * FROM stockHistory WHERE name = :name order by date ASC")
    fun getAll(name: String): MutableList<stockHistory>

    @Insert
    fun insertAll(vararg users: stockHistory)
}


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
