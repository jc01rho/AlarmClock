/*
 * Copyright (C) 2012 Yuriy Kulikov yuriy.kulikov.87@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.better.alarm.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.better.alarm.configuration.globalInject
import com.better.alarm.configuration.globalLogger
import com.better.alarm.interfaces.PresentationToModelIntents
import com.better.alarm.logger.Logger

class AlarmsReceiver : BroadcastReceiver() {
    private val alarms: Alarms by globalInject()
    private val log: Logger by globalLogger("AlarmsReceiver")

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AlarmsScheduler.ACTION_FIRED -> {
                val id = intent.getIntExtra(AlarmsScheduler.EXTRA_ID, -1)
                val calendarType = CalendarType.valueOf(intent.extras?.getString(AlarmsScheduler.EXTRA_TYPE)!!)
                log.d("Fired $id $calendarType")
                alarms.getAlarm(id)?.let {
                    alarms.onAlarmFired(it, calendarType)
                }
            }
            AlarmsScheduler.ACTION_INEXACT_FIRED -> {
                val id = intent.getIntExtra(AlarmsScheduler.EXTRA_ID, -1)
                log.d("Fired  ACTION_INEXACT_FIRED $id")
                alarms.getAlarm(id)?.let {
                    it.onInexactAlarmFired()
                }
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                log.d("Refreshing alarms because of ${intent.action}")
                alarms.refresh()
            }
            Intent.ACTION_TIME_CHANGED -> alarms.onTimeSet()

            PresentationToModelIntents.ACTION_REQUEST_SNOOZE -> {
                val id = intent.getIntExtra(AlarmsScheduler.EXTRA_ID, -1)
                log.d("Snooze $id")
                alarms.getAlarm(id)?.snooze()
            }

            PresentationToModelIntents.ACTION_REQUEST_DISMISS -> {
                val id = intent.getIntExtra(AlarmsScheduler.EXTRA_ID, -1)
                log.d("Dismiss $id")
                alarms.getAlarm(id)?.dismiss()
            }

            PresentationToModelIntents.ACTION_REQUEST_SKIP -> {
                val id = intent.getIntExtra(AlarmsScheduler.EXTRA_ID, -1)
                log.d("RequestSkip $id")
                alarms.getAlarm(id)?.requestSkip()
            }
        }
    }
}
