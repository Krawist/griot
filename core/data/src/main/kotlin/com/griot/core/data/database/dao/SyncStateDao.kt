package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.griot.core.data.database.entity.SyncStateEntity
import com.griot.core.data.model.SyncEntityType

@Dao
internal interface SyncStateDao {
    /** Records what sync exchanged for one row, replacing the previous state (no other table points to this one). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SyncStateEntity)

    /** What sync last exchanged for the row; `null` when it never synced. */
    @Query("SELECT * FROM sync_state WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun get(entityType: SyncEntityType, entityId: String): SyncStateEntity?
}
