package com.example.v_dev2.VDB

import androidx.lifecycle.LiveData
import androidx.room.*
import java.sql.Timestamp

@Dao
interface historyDao {
    @Query("SELECT * FROM stockHistory WHERE code = :code order by date DESC") // use LIKE to get todays data only
    fun historyLiveSelect(code: String) : LiveData<MutableList<stockHistory>>

    @Query("SELECT * FROM stockHistory WHERE code = :code order by date DESC")
    fun getAll(code: String): MutableList<stockHistory>

    @Insert
    fun insertAll(vararg users: stockHistory)

    @Query("DELETE FROM stockHistory WHERE code = :code")
    fun deleteall(code: String)
}


//data class vProfile(var vName:String,var vTheme:MutableList<String>,  val themeRank:Int, var vtime: String, @PrimaryKey var id: Int)
@Dao
interface vDao {
    @Query("SELECT * FROM vTable order by vtime DESC") // use LIKE to get todays data only
    fun chatLiveSelect() : LiveData<MutableList<vProfile>>

    @Query("SELECT * FROM vTable")
    fun getAll(): MutableList<vProfile>

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

@Dao
interface rankDao {
    @Query("SELECT * FROM rank WHERE rankType = :rankType order by rank ASC") // use LIKE to get todays data only
    fun rankLiveSelect(rankType: String) : LiveData<MutableList<rank>>

    @Query("SELECT * FROM rank")
    fun getAll(): MutableList<rank>
//
//    @Query("UPDATE rank SET rankName = :rankName, nameList = :nameList, valueList = :valueList WHERE id = 0")
//    fun updateRank(rankName:String, nameList:String, valueList:String)

    @Update
    fun update(rankList: MutableList<rank>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(rankList: MutableList<rank>)
}