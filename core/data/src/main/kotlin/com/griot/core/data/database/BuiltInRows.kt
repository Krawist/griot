package com.griot.core.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL

/**
 * Writes « Archives » and the built-in genres when they are missing, each time the database opens: on a new install
 * and after the upgrade that brings folders and genres (GRI-55). Their ids are fixed, so every device has the same
 * rows, and they are dated at the epoch, so that a synced row always wins.
 */
internal object BuiltInRows : RoomDatabase.Callback() {
    private val statements = listOf(
        """
        INSERT OR IGNORE INTO folders (id, name, color, kind, position, updatedAt, deleted)
        VALUES ('archives', '', 'NEUTRAL', 'ARCHIVES', 0, 0, 0)
        """,
    ) +
        listOf(
            "afro" to "AFRO",
            "rap" to "RAP",
            "rnb" to "RNB",
            "gospel" to "GOSPEL",
            "pop" to "POP",
            "other" to "OTHER",
        )
            .mapIndexed { position, (id, builtIn) ->
                """
            INSERT OR IGNORE INTO genres (id, name, builtIn, position, updatedAt, deleted)
            VALUES ('$id', '', '$builtIn', $position, 0, 0)
            """
            }

    // The app's database, on the framework's SQLite.
    override fun onOpen(db: SupportSQLiteDatabase) {
        statements.forEach(db::execSQL)
    }

    // A database on a driver, such as the tests' in-memory one.
    override fun onOpen(connection: SQLiteConnection) {
        statements.forEach(connection::execSQL)
    }
}
