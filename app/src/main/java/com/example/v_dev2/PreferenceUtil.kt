package com.example.v_dev2

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("filterSettings", Context.MODE_PRIVATE)

    fun getNextUID(): Int {
        return prefs.getInt("UID",0)
    }
    fun setNextUID(): Int {
        val uid = getNextUID()
        val nextUID = uid + 1
        prefs.edit().putInt("UID",nextUID).apply()
        return nextUID
    }
    fun getNextCID(): Int {
        return prefs.getInt("CID",1000)
    }
    fun setNextCID(): Int {
        val cid = getNextCID()
        val nextCID = cid + 1
        try {
            prefs.edit().remove("CID").apply()
        }finally {
            prefs.edit().putInt("CID" , nextCID).apply()
        }
        return nextCID
    }
    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }
    fun setString(key: String, str: String) {
        try{
            prefs.edit().remove(key).apply()
        }catch(e: Exception){}
        finally {
            prefs.edit().putString(key, str).apply()
        }
    }
    fun getPreflist(): MutableSet<String> {
        return prefs.getStringSet("prefList", mutableSetOf<String>())!!
    }
    fun addPrefList(prefName: String): String {
        val newPref = getPreflist()
        newPref.add(prefName)
        val uid = setNextUID().toString()
        prefs.edit().putStringSet("prefList", newPref).apply()
        return uid
    }
    fun delPref(prefName: String){
        val prefList = getPreflist()
        prefList.remove(prefName)
        prefs.edit().remove("prefList").apply()
        prefs.edit().putStringSet("prefList",prefList).apply()
        prefs.edit().remove(prefName).apply()
    }
    // 이름은 안바뀌고 값만 바뀔때
    fun changeProfileValue(prefName: String, str: String){
        prefs.edit().remove(prefName).apply()
        prefs.edit().putString(prefName, str).apply()
    }
    // 이름만 바뀔때
    fun changeProfileName(oldName: String, newName: String) {
        val tmpval = getString(oldName,"")
        setString(newName,tmpval)
        prefs.edit().remove(oldName).apply()
        val prefList = getPreflist()!!
        prefList.remove(oldName)
        prefList.add(newName)
        prefs.edit().putStringSet("prefList",prefList).apply()
    }
}
