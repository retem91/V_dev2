package com.example.v_dev2.VDB

import android.content.Context
import androidx.room.*
import androidx.room.Room.databaseBuilder

@Database(entities = arrayOf( vProfile::class, stockHistory::class, rank::class), version = 4,exportSchema = true)
abstract class VDB : RoomDatabase() {
//    abstract fun vDao(): VDao
    abstract fun vDao(): vDao
    abstract fun historyDao(): historyDao
    abstract fun rankDao(): rankDao

    companion object {
        private var INSTANCE: VDB? = null

        fun getInstance(context: Context): VDB? {
            if (INSTANCE == null) {
                synchronized(VDB::class) {
                    INSTANCE = databaseBuilder(context.applicationContext,
                        VDB::class.java, "V.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}