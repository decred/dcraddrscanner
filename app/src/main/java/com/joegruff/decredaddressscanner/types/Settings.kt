package com.joegruff.decredaddressscanner.types

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.runBlocking

const val SETTINGS_TABLE = "settings_table"
const val URL_FIELD = "url"
const val DEFAULT_USER = "default"

const val mainNet = "https://explorer.dcrdata.org/api/"
const val testNet = "https://testnet.dcrdata.org/api/"

@Entity(tableName = SETTINGS_TABLE)
data class Settings(
    @PrimaryKey val user: String,
    @ColumnInfo(name = URL_FIELD) var url: String = mainNet,
)

class UserSettings(private val settingsDao: SettingsDao, private val ctx: Context) {
    companion object {
        @Volatile
        private var settings: UserSettings? = null
        fun get(
            ctx: Context,
        ): UserSettings {
            if (settings != null) return settings as UserSettings
            val db = MyDatabase.get(ctx)
            settings = UserSettings(db.settingsDao(), ctx)
            return settings!!
        }
    }

    val settings = settings()

    private fun settings(): Settings {
        var setts: Settings? = null
        runBlocking {
            setts = settingsDao.get(DEFAULT_USER)
            if (setts == null) {
                setts = Settings(DEFAULT_USER)
                settingsDao.insert(setts!!)
            }
        }
        return setts as Settings
    }

    fun updateSettings(settings: Settings) {
        runBlocking {
            settingsDao.update(settings)
        }
    }

    suspend fun insert(settings: Settings) {
        settingsDao.insert(settings)
    }

}