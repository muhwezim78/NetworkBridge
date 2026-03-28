package com.muhwezi.networkbridge.data.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Events that affect the user's session globally.
 */
sealed class SessionEvent {
    /** The JWT token has expired or been rejected by the server (HTTP 401). */
    object Expired : SessionEvent()
}

/**
 * Application-wide session state manager.
 * Emits [SessionEvent]s that the UI layer (MainActivity) observes
 * to perform global navigation (e.g., redirect to login on token expiry).
 */
@Singleton
class SessionManager @Inject constructor() {

    private val _sessionEvents = MutableSharedFlow<SessionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Observe this from the UI layer to react to session changes. */
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    /**
     * Called by [AuthInterceptor] when a 401 is received on an authenticated request.
     * Emits [SessionEvent.Expired] so the UI navigates to login.
     */
    fun onSessionExpired() {
        _sessionEvents.tryEmit(SessionEvent.Expired)
    }
}
