package com.joegruff.decredaddressscanner.types

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM address_table")
    fun getAll(): Flow<List<Address>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(addr: Address)

    @Delete
    suspend fun delete(addr: Address)

    @Update
    suspend fun update(addr:Address);
}
