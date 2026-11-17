package com.griot.core.data.audio

import android.service.notification.NotificationListenerService

/**
 * Only there so that Android lets Griot read the media sessions of other apps (« accès aux notifications »): it never
 * reads a notification.
 *
 * Play Store: before any public release, this access needs the « Notification listener » declaration in the Play
 * Console, explaining that it only controls the music of other apps (GRI-53).
 */
internal class MusicListener : NotificationListenerService()
