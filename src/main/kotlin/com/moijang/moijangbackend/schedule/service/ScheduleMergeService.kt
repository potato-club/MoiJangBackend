package com.moijang.moijangbackend.schedule.service

import com.moijang.moijangbackend.schedule.dto.BusyTime
import com.moijang.moijangbackend.schedule.dto.FreeTime
import com.moijang.moijangbackend.schedule.entity.PersonalSchedule

object ScheduleMergeService {
    private const val SLOT_SECONDS = 30 * 60
    private const val DAY_SECONDS = 24 * 60 * 60

    data class MergeResult(
        val busyTimes: List<BusyTime>,
        val freeTimes: List<FreeTime>,
    )

    fun mergeDay(schedules: List<PersonalSchedule>): MergeResult {
        val schedulesByUser = schedules.groupBy { it.user.id }
        val busyTimes = mutableListOf<BusyTime>()
        val freeSlotStarts = mutableListOf<Int>()

        for (slotStart in 0 until DAY_SECONDS step SLOT_SECONDS) {
            val slotEnd = slotStart + SLOT_SECONDS
            val busyUserCount = schedulesByUser.values.count { userSchedules ->
                userSchedules.any { schedule ->
                    schedule.startTime.toSecondOfDay() < slotEnd &&
                        schedule.endTime.toSecondOfDay() > slotStart
                }
            }

            if (busyUserCount == 0) {
                freeSlotStarts += slotStart
            } else {
                busyTimes += BusyTime(
                    startTime = formatBoundary(slotStart),
                    endTime = formatBoundary(slotEnd),
                    busyUserCount = busyUserCount,
                )
            }
        }

        return MergeResult(
            busyTimes = busyTimes,
            freeTimes = mergeContiguousFreeSlots(freeSlotStarts),
        )
    }

    @Deprecated("Use mergeDay", ReplaceWith("mergeDay(schedules).busyTimes"))
    fun mergeBusyTimes(schedules: List<PersonalSchedule>): List<BusyTime> = mergeDay(schedules).busyTimes

    private fun mergeContiguousFreeSlots(slotStarts: List<Int>): List<FreeTime> {
        if (slotStarts.isEmpty()) {
            return emptyList()
        }

        val freeTimes = mutableListOf<FreeTime>()
        var rangeStart = slotStarts.first()
        var previous = slotStarts.first()

        for (slotStart in slotStarts.drop(1)) {
            if (slotStart == previous + SLOT_SECONDS) {
                previous = slotStart
                continue
            }
            freeTimes += FreeTime(
                startTime = formatBoundary(rangeStart),
                endTime = formatBoundary(previous + SLOT_SECONDS),
            )
            rangeStart = slotStart
            previous = slotStart
        }
        freeTimes += FreeTime(
            startTime = formatBoundary(rangeStart),
            endTime = formatBoundary(previous + SLOT_SECONDS),
        )
        return freeTimes
    }

    private fun formatBoundary(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = totalSeconds % 3600 / 60
        return "%02d:%02d".format(hours, minutes)
    }
}
