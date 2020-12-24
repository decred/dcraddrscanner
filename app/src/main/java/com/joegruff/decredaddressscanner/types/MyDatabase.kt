package com.joegruff.decredaddressscanner.types

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope


@Database(entities = [Address::class, Settings::class], version = 1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun addrDao(): AddressDao
    abstract fun settingsDao(): SettingsDao
    companion object {
        @Volatile
        private var mydb: MyDatabase? = null

        fun get( ctx: Context): MyDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return mydb ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext,
                    MyDatabase::class.java,
                    "dcraddrscannerdb"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    .fallbackToDestructiveMigration()
                    .build()
                mydb = instance
                // return instance
                instance
            }
        }
    }
}
