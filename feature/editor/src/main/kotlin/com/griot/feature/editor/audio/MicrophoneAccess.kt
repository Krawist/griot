package com.griot.feature.editor.audio

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.griot.core.design.component.GriotButton
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.R

/**
 * Asks for the microphone when it is [wanted], after a short explanation, then calls [onGranted]; [onDismiss] when the
 * author says no. Refused for good, the explanation offers the phone's settings. Writing never waits on it.
 */
@Composable
internal fun MicrophoneAccess(wanted: Boolean, onGranted: () -> Unit, onDismiss: () -> Unit) {
    if (!wanted) return
    val context = LocalContext.current
    if (context.hasMicrophone()) {
        onGranted()
        return
    }
    var refused by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onGranted()
        } else {
            // No dialog any more: only the settings can give the microphone back.
            refused =
                context.activity()?.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) == false
            if (!refused) onDismiss()
        }
    }
    GriotSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.microphone_title),
            style = GriotTheme.type.sheetTitle,
            color = GriotTheme.palette.ink,
        )
        Text(
            text = stringResource(if (refused) R.string.microphone_refused else R.string.microphone_body),
            style = GriotTheme.type.body,
            color = GriotTheme.palette.ink2,
        )
        GriotButton(
            text = stringResource(if (refused) R.string.microphone_settings else R.string.microphone_allow),
            onClick = {
                if (refused) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        ),
                    )
                    onDismiss()
                } else {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
        )
    }
}

private fun Context.hasMicrophone() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}
