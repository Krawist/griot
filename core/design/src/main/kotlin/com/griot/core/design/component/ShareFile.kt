package com.griot.core.design.component

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Offers the exported file at [path] to the apps that take a [mimeType] (messages, mail, notes), through the system's
 * share sheet titled [title]. The file must be in the cache's « exports » folder, served by the exports FileProvider.
 */
fun shareFile(context: Context, name: String, path: String, mimeType: String, title: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.exports", File(path))
    val send = Intent(Intent.ACTION_SEND)
        .setType(mimeType)
        .putExtra(Intent.EXTRA_STREAM, uri)
        .putExtra(Intent.EXTRA_TITLE, name)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, title))
}
