package com.moijang.moijangbackend.schedule.service

import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.schedule.dto.ConfirmScheduleRequest
import com.moijang.moijangbackend.schedule.dto.MergedSchedule
import com.moijang.moijangbackend.schedule.dto.MergedScheduleResponse
import com.moijang.moijangbackend.schedule.dto.PostScheduleRequest
import com.moijang.moijangbackend.schedule.dto.PostScheduleResponse
import com.moijang.moijangbackend.schedule.dto.ScheduleResponse
import com.moijang.moijangbackend.schedule.entity.PersonalSchedule
import com.moijang.moijangbackend.schedule.repository.PersonalScheduleRepository
import com.moijang.moijangbackend.schedule.validation.ScheduleValidator
import com.moijang.moijangbackend.team.entity.RoomType
import com.moijang.moijangbackend.team.entity.Team
import com.moijang.moijangbackend.team.repository.TeamRepository
import com.moijang.moijangbackend.team.repository.TeamUserRepository
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
    private val teamRepository: TeamRepository,
    private val teamUserRepository: TeamUserRepository,
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

        return PostScheduleResponse(scheduleId = schedule.id)
    }

    @Transactional(readOnly = true)
    fun getSchedules(userId: Long, year: Int, month: Int): List<ScheduleResponse> {
        findUser(userId)

        val yearMonth = YearMonth.of(year, month)
        val datedSchedules = personalScheduleRepository.findAllByUser_IdAndDateBetween(
            userId = userId,
            start = yearMonth.atDay(1),
            end = yearMonth.atEndOfMonth(),
        ).map(::toScheduleResponse)

        val expandedRepeating = personalScheduleRepository.findAllByUser_IdAndIsRepeatingTrue(userId)
            .flatMap { schedule ->
                val dayOfWeek = schedule.dayOfWeek ?: return@flatMap emptyList()
                datesInMonth(yearMonth, dayOfWeek).map { date ->
                    toScheduleResponse(schedule).copy(
                        date = date.toString(),
                        dayOfWeek = dayOfWeek.name,
                    )
                }
            }

        return (datedSchedules + expandedRepeating)
            .sortedWith(
                compareBy<ScheduleResponse>(
                    { it.date },
                    { it.startTime },
                    { it.scheduleId },
                ),
            )
    }

    @Transactional(readOnly = true)
    fun getMergedTeamSchedules(userId: Long, teamId: Long): MergedScheduleResponse {
        val team = teamRepository.findById(teamId).orElseThrow {
            BusinessException(ErrorCode.TEAM_NOT_FOUND)
        }
        if (!teamUserRepository.existsByTeam_IdAndUser_Id(teamId, userId)) {
            throw BusinessException(ErrorCode.TEAM_FORBIDDEN)
        }

        val memberIds = teamUserRepository.findUserIdsByTeamId(teamId)
        val mergedSchedules = when (team.roomType) {
            RoomType.SHORT_TERM -> mergeShortTermSchedules(team, memberIds)
            RoomType.RECURRING -> mergeRecurringSchedules(memberIds)
        }

        return MergedScheduleResponse(
            teamId = team.id,
            roomType = team.roomType,
            mergedSchedules = mergedSchedules,
        )
    }

    @Transactional
    fun confirmTeamSchedule(userId: Long, teamId: Long, request: ConfirmScheduleRequest) {
        val team = teamRepository.findById(teamId).orElseThrow {
            BusinessException(ErrorCode.TEAM_NOT_FOUND)
        }
        if (team.leader.id != userId) {
            throw BusinessException(ErrorCode.TEAM_FORBIDDEN)
        }

        val confirmedDate = LocalDate.parse(request.confirmedDate)
        if (confirmedDate.isBefore(team.startDate) || confirmedDate.isAfter(team.endDate)) {
            throw BusinessException(ErrorCode.SCHEDULE_OUTSIDE_TEAM_PERIOD)
        }
        val startTime = LocalTime.parse(request.startTime)
        val endTime = LocalTime.parse(request.endTime)
        ScheduleValidator.validate(
            isRepeating = false,
            date = confirmedDate,
            dayOfWeek = null,
            startTime = startTime,
            endTime = endTime,
        )

        personalScheduleRepository.deleteAllBySourceTeam_Id(teamId)

        val confirmedSchedules = teamUserRepository.findAllByTeam_Id(teamId).map { teamUser ->
            PersonalSchedule(
                user = teamUser.user,
                title = request.eventTitle,
                categoryColor = TEAM_CONFIRMED_CATEGORY_COLOR,
                isRepeating = false,
                date = confirmedDate,
                startTime = startTime,
                endTime = endTime,
                sourceTeam = team,
            )
        }
        personalScheduleRepository.saveAll(confirmedSchedules)
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

    private fun datesInMonth(yearMonth: YearMonth, dayOfWeek: DayOfWeek): List<LocalDate> {
        return generateSequence(yearMonth.atDay(1)) { date ->
            date.plusDays(1).takeIf { it.month == yearMonth.month }
        }.filter { it.dayOfWeek == dayOfWeek }.toList()
    }

    private fun mergeShortTermSchedules(team: Team, memberIds: List<Long>): List<MergedSchedule> {
        if (memberIds.isEmpty()) {
            return emptyList()
        }

        val datedSchedules = personalScheduleRepository.findAllDatedByUserIdsAndDateBetween(
            userIds = memberIds,
            start = team.startDate,
            end = team.endDate,
        ).groupBy { it.date }
        val repeatingSchedules = personalScheduleRepository.findAllRepeatingByUserIds(memberIds)
            .groupBy { it.dayOfWeek }

        return generateSequence(team.startDate) { date ->
            date.plusDays(1).takeIf { !it.isAfter(team.endDate) }
        }.map { date ->
            val schedules = datedSchedules[date].orEmpty() + repeatingSchedules[date.dayOfWeek].orEmpty()
            val merged = ScheduleMergeService.mergeDay(schedules)
            MergedSchedule(
                date = date.toString(),
                dayOfWeek = date.dayOfWeek.name,
                busyTimes = merged.busyTimes,
                freeTimes = merged.freeTimes,
            )
        }.toList()
    }

    private fun mergeRecurringSchedules(memberIds: List<Long>): List<MergedSchedule> {
        if (memberIds.isEmpty()) {
            return emptyList()
        }

        val schedulesByDay = personalScheduleRepository.findAllRepeatingByUserIds(memberIds)
            .groupBy { it.dayOfWeek }

        return DayOfWeek.entries.map { dayOfWeek ->
            val merged = ScheduleMergeService.mergeDay(schedulesByDay[dayOfWeek].orEmpty())
            MergedSchedule(
                date = null,
                dayOfWeek = dayOfWeek.name,
                busyTimes = merged.busyTimes,
                freeTimes = merged.freeTimes,
            )
        }
    }

    private data class ParsedSchedule(
        val date: LocalDate?,
        val dayOfWeek: DayOfWeek?,
        val startTime: LocalTime,
        val endTime: LocalTime,
    )

    private companion object {
        const val TEAM_CONFIRMED_CATEGORY_COLOR = "#4A90E2"
    }
}
