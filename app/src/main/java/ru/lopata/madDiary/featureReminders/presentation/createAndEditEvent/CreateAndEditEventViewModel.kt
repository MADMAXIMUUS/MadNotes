package ru.lopata.madDiary.featureReminders.presentation.createAndEditEvent

import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.lopata.madDiary.R
import ru.lopata.madDiary.core.util.EditTextState
import ru.lopata.madDiary.core.util.UiEvent
import ru.lopata.madDiary.featureReminders.domain.model.Event
import ru.lopata.madDiary.featureReminders.domain.model.EventRepeatAttachment
import ru.lopata.madDiary.featureReminders.domain.model.Repeat
import ru.lopata.madDiary.featureReminders.domain.useCase.event.EventUseCases
import java.sql.Date
import javax.inject.Inject

@HiltViewModel
class CreateAndEditEventViewModel @Inject constructor(
    private val eventUseCases: EventUseCases,
    state: SavedStateHandle
) : ViewModel() {

    private val _currentEvent = MutableStateFlow(CreateEventScreenState())
    val currentEvent = _currentEvent.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var preEditEvent: Event = Event()
    private var preEditRepeat: Repeat = Repeat()

    init {
        val calendarStart = Calendar.getInstance()
        calendarStart.timeZone = TimeZone.getDefault()
        val calendarEnd = Calendar.getInstance()
        calendarEnd.timeZone = TimeZone.getDefault()
        calendarEnd.timeInMillis = calendarStart.timeInMillis
        calendarEnd.add(Calendar.HOUR_OF_DAY, 1)
        val hourStart = calendarStart.get(Calendar.HOUR_OF_DAY)
        val minuteStart = calendarStart.get(Calendar.MINUTE)
        val startTime = hourStart * 60L * 60L * 1000L + minuteStart * 60L * 1000L
        val hourEnd = calendarEnd.get(Calendar.HOUR_OF_DAY)
        val minuteEnd = calendarEnd.get(Calendar.MINUTE)
        val endTime = hourEnd * 60L * 60L * 1000L + minuteEnd * 60L * 1000L
        _currentEvent.update { currentValue ->
            currentValue.copy(
                startDate = calendarStart.apply {
                    set(Calendar.HOUR, 0)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.MILLISECONDS_IN_DAY, 0)
                }.timeInMillis,
                startTime = startTime,
                endDate = calendarEnd.apply {
                    set(Calendar.HOUR, 0)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.MILLISECONDS_IN_DAY, 0)
                }.timeInMillis,
                endTime = endTime,
            )
        }
        state.get<EventRepeatAttachment>("eventAndRepeat")?.let { eventAndRepeat ->
            viewModelScope.launch {
                val event = eventAndRepeat.event
                preEditEvent = event
                val repeat = eventAndRepeat.repeat ?: Repeat()
                preEditRepeat = repeat
                calendarStart.timeInMillis = event.startDateTime.time
                calendarStart.apply {
                    set(Calendar.HOUR, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.MILLISECONDS_IN_DAY, 0)
                }
                calendarEnd.timeInMillis = event.endDateTime.time
                calendarEnd.apply {
                    set(Calendar.HOUR, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.MILLISECONDS_IN_DAY, 0)
                }
                _currentEvent.update { currentValue ->
                    currentValue.copy(
                        title = EditTextState(
                            text = event.title,
                            isEmpty = false,
                            isError = false
                        ),
                        completed = event.completed,
                        startDate = calendarStart.timeInMillis,
                        startTime = event.startDateTime.time - calendarStart.timeInMillis,
                        endDate = calendarEnd.timeInMillis,
                        endTime = event.endDateTime.time - calendarEnd.timeInMillis,
                        allDay = event.allDay,
                        color = event.color,
                        location = event.location,
                        note = event.note,
                        repeat = repeat.repeatInterval,
                        repeatEnd = repeat.repeatEnd,
                        id = event.eventId
                    )
                }
                when (repeat.repeatInterval) {
                    Repeat.NO_REPEAT -> {
                        _currentEvent.update { currentValue ->
                            currentValue.copy(
                                repeatTitle = R.string.never
                            )
                        }
                    }
                    Repeat.EVERY_DAY -> {
                        _currentEvent.update { currentValue ->
                            currentValue.copy(
                                repeatTitle = R.string.every_day
                            )
                        }
                    }
                    Repeat.EVERY_SECOND_DAY -> {
                        _currentEvent.update { currentValue ->
                            currentValue.copy(
                                repeatTitle = R.string.every_second_day
                            )
                        }
                    }
                    Repeat.EVERY_WEEK -> {
                        _currentEvent.update { currentValue ->
                            currentValue.copy(
                                repeatTitle = R.string.every_week
                            )
                        }
                    }
                    Repeat.EVERY_SECOND_WEEK -> {
                        _currentEvent.update { currentValue ->
                            currentValue.copy(
                                repeatTitle = R.string.every_second_week
                            )
                        }
                    }
                    Repeat.EVERY_MONTH -> {
                        _currentEvent.update { currentValue ->
                            currentValue.copy(
                                repeatTitle = R.string.every_month
                            )
                        }
                    }
                    Repeat.EVERY_YEAR -> {
                        _currentEvent.update { currentValue ->
                            currentValue.copy(
                                repeatTitle = R.string.every_year
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateColor(value: Int) {
        _currentEvent.update { currentValue ->
            currentValue.copy(
                color = value
            )
        }
    }

    fun updateStartDate(value: Long) {
        if (value <= currentEvent.value.endDate || currentEvent.value.endDate == 0L) {
            _currentEvent.value = currentEvent.value.copy(
                startDate = value,
                isStartDateError = false
            )
        }
    }

    fun updateStartTime(value: Long) {
        _currentEvent.value = currentEvent.value.copy(
            startTime = value,
            isStartDateError = false
        )
    }

    fun updateEndDate(value: Long) {
        if (value >= currentEvent.value.startDate) {
            _currentEvent.value = currentEvent.value.copy(
                endDate = value,
                isEndDateError = false
            )
        }
    }

    fun updateEndTime(value: Long) {
        _currentEvent.value = currentEvent.value.copy(
            endTime = value,
            isEndDateError = false
        )
    }

    fun updateNote(note: String) {
        _currentEvent.value = currentEvent.value.copy(
            note = note
        )
    }

    fun updateAllDayState(state: Boolean) {
        if (state) {
            _currentEvent.value = currentEvent.value.copy(
                startTime = 0L,
                endTime = 0L
            )
        }
        _currentEvent.value = currentEvent.value.copy(
            allDay = state
        )
    }

    fun updateRepeat(repeat: Long) {
        _currentEvent.value = currentEvent.value.copy(
            repeat = repeat
        )
    }

    fun updateRepeatTitle(@StringRes value: Int) {
        _currentEvent.value = currentEvent.value.copy(
            repeatTitle = value
        )
    }

    fun updateNotificationTitle(title: IntArray) {
        _currentEvent.value = currentEvent.value.copy(
            notificationTitle = title.toList()
        )
    }

    fun updateNotifications(notifications: LongArray) {
        _currentEvent.value = currentEvent.value.copy(
            notifications = notifications.toList()
        )
    }

    fun updateTitle(title: String) {
        val currentText = currentEvent.value.title.text
        if (title != currentText) {
            _currentEvent.value = currentEvent.value.copy(
                title = currentEvent.value.title.copy(
                    text = title,
                    isEmpty = title.isEmpty()
                )
            )
            if (title.isNotEmpty()) {
                _currentEvent.value = currentEvent.value.copy(
                    title = currentEvent.value.title.copy(
                        isError = false
                    )
                )
            }
        }
    }

    fun deleteEvent() {
        if (preEditEvent != Event()) {
            viewModelScope.launch {
                eventUseCases.deleteEventUseCase(
                    preEditEvent
                )
                _eventFlow.emit(UiEvent.Delete)
            }
        }
    }

    fun saveEvent() {
        viewModelScope.launch {
            if (!currentEvent.value.title.isEmpty
                && currentEvent.value.startDate != 0L
                && currentEvent.value.endDate != 0L
            ) {
                if (currentEvent.value.id == null) {
                    val id = eventUseCases.createEventUseCase(
                        Event(
                            title = currentEvent.value.title.text,
                            startDateTime = Date(currentEvent.value.startDate + currentEvent.value.startTime),
                            endDateTime = Date(currentEvent.value.endDate + currentEvent.value.endTime),
                            allDay = currentEvent.value.allDay,
                            color = currentEvent.value.color,
                            completed = currentEvent.value.completed,
                            location = currentEvent.value.location,
                            note = currentEvent.value.note,
                        )
                    )
                    eventUseCases.createRepeatUseCase(
                        Repeat(
                            repeatStart = Date(currentEvent.value.startDate + currentEvent.value.startTime),
                            repeatInterval = currentEvent.value.repeat,
                            eventOwnerId = id.toInt()
                        )
                    )
                } else {
                    eventUseCases.createEventUseCase(
                        Event(
                            eventId = currentEvent.value.id,
                            title = currentEvent.value.title.text,
                            startDateTime = Date(currentEvent.value.startDate + currentEvent.value.startTime),
                            endDateTime = Date(currentEvent.value.endDate + currentEvent.value.endTime),
                            allDay = currentEvent.value.allDay,
                            color = currentEvent.value.color,
                            completed = currentEvent.value.completed,
                            location = currentEvent.value.location,
                            note = currentEvent.value.note,
                        )
                    )
                    eventUseCases.createRepeatUseCase(
                        Repeat(
                            repeatStart = Date(currentEvent.value.startDate + currentEvent.value.startTime),
                            repeatInterval = currentEvent.value.repeat,
                            eventOwnerId = currentEvent.value.id!!
                        )
                    )
                }
                _eventFlow.emit(UiEvent.Save)
            } else {
                if (currentEvent.value.title.isEmpty) {
                    _currentEvent.value = currentEvent.value.copy(
                        title = currentEvent.value.title.copy(isError = true)
                    )
                }
                if (currentEvent.value.startDate == 0L) {
                    _currentEvent.value = currentEvent.value.copy(
                        isStartDateError = true
                    )
                }
                if (currentEvent.value.endDate == 0L) {
                    _currentEvent.value = currentEvent.value.copy(
                        isEndDateError = true
                    )
                }
            }
        }
    }
}