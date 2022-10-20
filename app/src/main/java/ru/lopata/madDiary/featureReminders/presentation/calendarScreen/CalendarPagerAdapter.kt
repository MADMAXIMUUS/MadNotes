package ru.lopata.madDiary.featureReminders.presentation.calendarScreen

import android.content.Context
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.lopata.madDiary.databinding.CalendarGridBinding
import ru.lopata.madDiary.featureReminders.presentation.calendarScreen.states.CalendarItemState
import ru.lopata.madDiary.featureReminders.presentation.calendarView.MadCalendarMonth

class CalendarPagerAdapter(
    private val context: Context
) : ListAdapter<CalendarItemState, CalendarPagerAdapter.CalendarPagerHolder>(DiffCallback()) {

    private var dayClickedListener: OnDayClickListener? = null

    override fun onViewAttachedToWindow(holder: CalendarPagerHolder) {
        super.onViewAttachedToWindow(holder)
        holder.setSelectedListener(object : CalendarPagerHolder.OnDayClickListener {
            override fun onDayCLick(view: MadCalendarMonth, day: Calendar) {
                dayClickedListener?.onDayCLick(view, day)
                holder.setSelectedDay(day.get(Calendar.DAY_OF_MONTH))
            }
        })
        holder.setSelectedDay(-1)
    }

    override fun onViewDetachedFromWindow(holder: CalendarPagerHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.setSelectedListener(null)
    }

    fun setOnDayClickListener(listener: OnDayClickListener) {
        dayClickedListener = listener
    }

    interface OnDayClickListener {
        fun onDayCLick(view: MadCalendarMonth, day: Calendar)
    }

    class DiffCallback : DiffUtil.ItemCallback<CalendarItemState>() {
        override fun areItemsTheSame(oldItem: CalendarItemState, newItem: CalendarItemState) =
            oldItem.monthNumber == newItem.monthNumber && oldItem.yearNumber == newItem.yearNumber

        override fun areContentsTheSame(oldItem: CalendarItemState, newItem: CalendarItemState) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarPagerHolder {
        val binding = CalendarGridBinding.inflate(LayoutInflater.from(context), parent, false)
        return CalendarPagerHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarPagerHolder, position: Int) {
        val month = getItem(position)
        holder.bind(month)
    }

    class CalendarPagerHolder(
        private val binding: CalendarGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var dayClickedListener: OnDayClickListener? = null

        fun bind(calendarItemState: CalendarItemState) {
            binding.calendar.apply {
                setMonthParams(
                    year = calendarItemState.yearNumber,
                    month = calendarItemState.monthNumber,
                    selectedDay = calendarItemState.initialSelectedDay,
                    weekStart = Calendar.MONDAY
                )
                setEventsOnMonth(calendarItemState.events)
                setOnDayClickListener(object : MadCalendarMonth.OnDayClickListener {
                    override fun onDayClick(view: MadCalendarMonth, day: Calendar) {
                        this@apply.setSelectedDay(day.get(Calendar.DAY_OF_MONTH))
                        dayClickedListener?.onDayCLick(view, day)
                    }
                })
            }
        }

        fun setSelectedListener(listener: OnDayClickListener?) {
            dayClickedListener = listener
        }

        fun setSelectedDay(day: Int) {
            binding.calendar.setSelectedDay(day)
        }

        interface OnDayClickListener {
            fun onDayCLick(view: MadCalendarMonth, day: Calendar)
        }
    }
}