/*
 * Copyright (c) 2021 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.mobile.android.vpn.health

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.duckduckgo.mobile.android.vpn.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class HealthNotificationManager(private val context: Context) {

    var shouldShowNotifications: Boolean = true

    suspend fun showBadHealthNotification() {
        if (!shouldShowNotifications) {
            Timber.v("Not showing health notifications.")
            return
        }

        val target = Intent().also {
            it.setClassName(context.packageName, "dummy.ui.VpnDiagnosticsActivity")
        }

        val pendingIntent = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(target)
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, "notificationid")
            .setSmallIcon(R.drawable.ic_baseline_mood_bad_24)
            .setContentTitle("hello")
            .setContentText("it looks like the VPN Service might be in bad health")
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()

        withContext(Dispatchers.Main) {
            NotificationManagerCompat.from(context).let { nm ->

                val channelBuilder =
                    NotificationChannelCompat.Builder("notificationid", NotificationManagerCompat.IMPORTANCE_DEFAULT)
                        .setName("notificationid")
                nm.createNotificationChannel(channelBuilder.build())

                nm.notify(AppTPHealthMonitor.BAD_HEALTH_NOTIFICATION_ID, notification)
            }
        }
    }

    fun hideBadHealthNotification() {
        NotificationManagerCompat.from(context).cancel(AppTPHealthMonitor.BAD_HEALTH_NOTIFICATION_ID)
    }
}
