package com.decred.decredaddressscanner.types

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [Address::class, Settings::class], version = 2, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun addrDao(): AddressDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var mydb: MyDatabase? = null

        fun get(ctx: Context): MyDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return mydb ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext,
                    MyDatabase::class.java,
                    "dcraddrscannerdb"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                mydb = instance
                // return instance
                instance
            }
        }
    }
}

// Adds the dcrdata seen boolean to settings.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE settings_table ADD COLUMN dcrdata_warning_accepted INTEGER NOT NULL DEFAULT 0;")
    }
}



