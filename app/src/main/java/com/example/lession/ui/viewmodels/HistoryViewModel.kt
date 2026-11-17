package com.example.lession.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lession.data.db.WorkoutDatabase
import com.example.lession.data.db.WorkoutSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WorkoutDatabase.getInstance(application)
    private val sessionDao = db.workoutSessionDao()

    val sessions: StateFlow<List<WorkoutSession>> = sessionDao.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun deleteSession(session: WorkoutSession) {
        viewModelScope.launch {
            sessionDao.deleteSession(session)
        }
    }
}
