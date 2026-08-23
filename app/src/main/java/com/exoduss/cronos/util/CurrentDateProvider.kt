package com.exoduss.cronos.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fonte única da "data de hoje" reativa.
 *
 * Emite [LocalDate.now] imediatamente e reemite a cada virada de meia-noite, para que
 * classificações dependentes de data (atrasadas/hoje/futuras) atualizem sem depender de
 * mudanças no banco. Extraído de HomeViewModel/NotificationsViewModel, que duplicavam esta lógica.
 */
@Singleton
class CurrentDateProvider @Inject constructor() {
    val currentDate: Flow<LocalDate> = flow {
        while (true) {
            emit(LocalDate.now())
            val secondsUntilMidnight = LocalTime.now().let {
                (23 - it.hour) * 3600L + (59 - it.minute) * 60L + (60 - it.second)
            }
            delay((secondsUntilMidnight + 1) * 1000L)
        }
    }
}
