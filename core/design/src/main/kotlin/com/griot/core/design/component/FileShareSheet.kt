package com.griot.core.design.component

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.FileProvider
import com.griot.core.design.R
import com.griot.core.design.theme.GriotTheme
import java.io.File

/**
 * What to do with an exported file: share it with another app, or save it in a folder the user picks.
 *
 * @param name The file's name, kept when saved.
 * @param path Its absolute path, in the cache's « exports » folder.
 * @param onSave Called with the document the user picked to save it to.
 */
@Composable
fun FileShareSheet(name: String, path: String, mimeType: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val palette = GriotTheme.palette
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.export_share)
    val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mimeType)) { uri ->
        if (uri != null) onSave(uri.toString())
        onDismiss()
    }
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.export_title),
            style = GriotTheme.type.sheetTitle,
            color = palette.ink,
        )
        Text(
            text = name,
            style = GriotTheme.type.description,
            color = palette.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        SheetAction(icon = R.drawable.ic_share, label = shareTitle, onClick = {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.exports", File(path))
            val send = Intent(Intent.ACTION_SEND)
                .setType(mimeType)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .putExtra(Intent.EXTRA_TITLE, name)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(send, shareTitle))
            onDismiss()
        })
        SheetAction(icon = R.drawable.ic_folder, label = stringResource(R.string.export_save), onClick = {
            saver.launch(name)
        })
    }
}
