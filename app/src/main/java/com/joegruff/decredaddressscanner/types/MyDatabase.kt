package com.joegruff.decredaddressscanner.types

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Address::class], version = 1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun addrDao(): AddressDao
    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): MyDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "address_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
