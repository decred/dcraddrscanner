package com.joegruff.decredaddressscanner.types

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

const val SETTINGS_TABLE = "settings_table"
const val URL_FIELD = "url"
const val DCRDATA_WARNING_ACCEPTED_FIELD = "dcrdata_warning_accepted"

// DEFAULT_USER is the only user.
const val DEFAULT_USER = "default"

const val dcrdataMainNet = "https://explorer.dcrdata.org/api/"
const val dcrdataTestNet = "https://testnet.dcrdata.org/api/"

@Entity(tableName = SETTINGS_TABLE)
data class Settings(
    @PrimaryKey val user: String,
    @ColumnInfo(name = URL_FIELD) var url: String = dcrdataMainNet,
    @ColumnInfo(name = DCRDATA_WARNING_ACCEPTED_FIELD) var dcrdataWarningAccepted: Boolean = false
)

class UserSettings(private val settingsDao: SettingsDao) : CoroutineScope {
    companion object {
        @Volatile
        private var settings: UserSettings? = null
        fun get(
            ctx: Context,
        ): UserSettings {
            return settings ?: synchronized(this) {
                val db = MyDatabase.get(ctx)
                val instance = UserSettings(db.settingsDao())
                settings = instance
                instance
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    private suspend fun settings(): Settings {
        var setts = settingsDao.get(DEFAULT_USER)
        if (setts != null) return setts
        setts = Settings(DEFAULT_USER)
        settingsDao.insert(setts)
        return setts
    }

    fun url(): String {
        var url: String
        runBlocking {
            url = settings().url
        }
        return url
    }

    fun setUrl(str: String) {
        suspend fun set(setts: Settings) {
            setts.url = str
            settingsDao.update(setts)
        }
        launch {
            set(settings())
        }
    }

    fun dcrdataWarningAccepted(): Boolean {
        var seen: Boolean
        runBlocking {
            seen = settings().dcrdataWarningAccepted
        }
        return seen
    }

    fun setDcrdataWarningAccepted() {
        suspend fun set(setts: Settings) {
            setts.dcrdataWarningAccepted = true
            settingsDao.update(setts)
        }
        launch {
            set(settings())
        }
    }
}