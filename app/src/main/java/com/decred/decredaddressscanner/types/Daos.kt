package com.decred.decredaddressscanner.types

import androidx.room.*

@Dao
interface AddressDao {
    // Order to ensure addresses always appear in order of time created.
    @Query("SELECT * FROM address_table ORDER BY timestamp_create ASC")
    suspend fun getAll(): List<Address>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(addr: Address)

    @Delete
    suspend fun delete(addr: Address)

    @Update
    suspend fun update(addr: Address)
}


@Dao
interface SettingsDao {
    // Currently there is only one user "default".
    @Query("SELECT * FROM settings_table where user=:user")
    suspend fun get(user: String): Settings?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(settings: Settings)

    @Update
    suspend fun update(settings: Settings)
}