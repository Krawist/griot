package com.griot.core.data.database

import androidx.room.TypeConverter
import java.time.Instant

/** Column types Room does not store natively. */
internal class DatabaseConverters {
    @TypeConverter
    fun instantToMillis(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun millisToInstant(millis: Long?): Instant? = millis?.let(Instant::ofEpochMilli)
}
