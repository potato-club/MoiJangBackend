package com.moijang.moijangbackend.schedule.service

import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.schedule.dto.GetScheduleResponse
import com.moijang.moijangbackend.schedule.dto.PostScheduleRequest
import com.moijang.moijangbackend.schedule.dto.PostScheduleResponse
import com.moijang.moijangbackend.schedule.dto.ScheduleResponse
import com.moijang.moijangbackend.schedule.entity.PersonalSchedule
import com.moijang.moijangbackend.schedule.repository.PersonalScheduleRepository
import com.moijang.moijangbackend.schedule.validation.ScheduleValidator
import com.moijang.moijangbackend.user.entity.User
import com.moijang.moijangbackend.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Service
class ScheduleService(
    private val personalScheduleRepository: PersonalScheduleRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun createSchedule(userId: Long, request: PostScheduleRequest): PostScheduleResponse {
        val user = findUser(userId)
        val parsed = parseScheduleRequest(request)
        validateSchedule(request.isRepeating, parsed)

        val schedule = personalScheduleRepository.save(
            PersonalSchedule(
                user = user,
                title = request.title,
                categoryColor = request.categoryColor,
                isRepeating = request.isRepeating,
                date = parsed.date,
                dayOfWeek = parsed.dayOfWeek,
                startTime = parsed.startTime,
                endTime = parsed.endTime,
            ),
        )

        return PostScheduleResponse(
            scheduleId = schedule.id,
            message = "일정이 등록되었습니다",
        )
    }

    @Transactional(readOnly = true)
    fun getSchedules(userId: Long, year: Int, month: Int): GetScheduleResponse {
        findUser(userId)

        val yearMonth = YearMonth.of(year, month)
        val datedSchedules = personalScheduleRepository.findAllByUser_IdAndDateBetween(
            userId = userId,
            start = yearMonth.atDay(1),
            end = yearMonth.atEndOfMonth(),
        )
        val repeatingSchedules = personalScheduleRepository.findAllByUser_IdAndIsRepeatingTrue(userId)

        val schedules = (datedSchedules + repeatingSchedules)
            .distinctBy { it.id }
            .map(::toScheduleResponse)

        return GetScheduleResponse(schedules = schedules)
    }

    @Transactional
    fun updateSchedule(userId: Long, scheduleId: Long, request: PostScheduleRequest) {
        val schedule = findOwnedSchedule(userId, scheduleId)
        val parsed = parseScheduleRequest(request)
        validateSchedule(request.isRepeating, parsed)

        schedule.updateContent(
            title = request.title,
            categoryColor = request.categoryColor,
            isRepeating = request.isRepeating,
            date = parsed.date,
            dayOfWeek = parsed.dayOfWeek,
            startTime = parsed.startTime,
            endTime = parsed.endTime,
        )
    }

    @Transactional
    fun deleteSchedule(userId: Long, scheduleId: Long) {
        val schedule = findOwnedSchedule(userId, scheduleId)
        personalScheduleRepository.delete(schedule)
    }

    private fun findUser(userId: Long): User {
        return userRepository.findById(userId).orElseThrow {
            BusinessException(ErrorCode.USER_NOT_FOUND)
        }
    }

    private fun findOwnedSchedule(userId: Long, scheduleId: Long): PersonalSchedule {
        return personalScheduleRepository.findByIdAndUser_Id(scheduleId, userId)
            ?: throw BusinessException(ErrorCode.SCHEDULE_NOT_FOUND)
    }

    private fun validateSchedule(isRepeating: Boolean, parsed: ParsedSchedule) {
        ScheduleValidator.validate(
            isRepeating = isRepeating,
            date = parsed.date,
            dayOfWeek = parsed.dayOfWeek,
            startTime = parsed.startTime,
            endTime = parsed.endTime,
        )
    }

    private fun parseScheduleRequest(request: PostScheduleRequest): ParsedSchedule {
        val date = request.date?.let { LocalDate.parse(it) }
        val dayOfWeek = request.dayOfWeek?.let { DayOfWeek.valueOf(it.uppercase()) }
        val startTime = LocalTime.parse(request.startTime)
        val endTime = LocalTime.parse(request.endTime)

        return ParsedSchedule(
            date = date,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
        )
    }

    private fun toScheduleResponse(schedule: PersonalSchedule): ScheduleResponse {
        return ScheduleResponse(
            scheduleId = schedule.id,
            title = schedule.title,
            categoryColor = schedule.categoryColor,
            isRepeating = schedule.isRepeating,
            date = schedule.date?.toString(),
            dayOfWeek = schedule.dayOfWeek?.name,
            startTime = schedule.startTime.toString(),
            endTime = schedule.endTime.toString(),
            sourceTeamId = schedule.sourceTeam?.id,
        )
    }

    private data class ParsedSchedule(
        val date: LocalDate?,
        val dayOfWeek: DayOfWeek?,
        val startTime: LocalTime,
        val endTime: LocalTime,
    )
}
