package com.moijang.moijangbackend.availability.service

import com.moijang.moijangbackend.availability.dto.AvailabilitySlotRequest
import com.moijang.moijangbackend.availability.dto.AvailabilitySlotSummary
import com.moijang.moijangbackend.availability.dto.AvailabilitySummaryResponse
import com.moijang.moijangbackend.availability.dto.AvailabilityUserSummary
import com.moijang.moijangbackend.availability.entity.Availability
import com.moijang.moijangbackend.availability.repository.AvailabilityRepository
import com.moijang.moijangbackend.availability.validation.AvailabilityValidator
import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.team.entity.Team
import com.moijang.moijangbackend.team.repository.TeamRepository
import com.moijang.moijangbackend.team.repository.TeamUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Service
class AvailabilityService(
    private val availabilityRepository: AvailabilityRepository,
    private val teamRepository: TeamRepository,
    private val teamUserRepository: TeamUserRepository,
) {

    @Transactional
    fun replaceMyAvailabilities(userId: Long, teamId: Long, requests: List<AvailabilitySlotRequest>) {
        val team = findTeam(teamId)
        val teamUser = teamUserRepository.findByTeam_IdAndUser_Id(teamId, userId)
            ?: throw BusinessException(ErrorCode.TEAM_FORBIDDEN)
        val slots = requests.map { parseAndValidate(team, it) }.distinct()

        availabilityRepository.deleteAllByTeam_IdAndUser_Id(teamId, userId)
        availabilityRepository.saveAll(
            slots.map { slot ->
                Availability(
                    team = team,
                    user = teamUser.user,
                    date = slot.date,
                    dayOfWeek = slot.dayOfWeek,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                )
            },
        )
    }

    @Transactional(readOnly = true)
    fun getAvailabilitySummary(userId: Long, teamId: Long): AvailabilitySummaryResponse {
        val team = findTeam(teamId)
        if (!teamUserRepository.existsByTeam_IdAndUser_Id(teamId, userId)) {
            throw BusinessException(ErrorCode.TEAM_FORBIDDEN)
        }

        val slots = availabilityRepository.findAllWithUserByTeamId(teamId)
            .groupBy { availability ->
                SlotKey(
                    date = availability.date,
                    dayOfWeek = availability.dayOfWeek,
                    startTime = availability.startTime,
                    endTime = availability.endTime,
                )
            }
            .entries
            .sortedWith(
                compareBy<Map.Entry<SlotKey, List<Availability>>>(
                    { it.key.date ?: LocalDate.MAX },
                    { it.key.dayOfWeek?.value ?: Int.MAX_VALUE },
                    { it.key.startTime },
                    { it.key.endTime },
                ),
            )
            .map { (slot, availabilities) ->
                AvailabilitySlotSummary(
                    date = slot.date?.toString(),
                    dayOfWeek = slot.dayOfWeek?.name,
                    startTime = slot.startTime.toString(),
                    endTime = slot.endTime.toString(),
                    selectedUsers = availabilities
                        .distinctBy { it.user.id }
                        .sortedBy { it.user.id }
                        .map {
                            AvailabilityUserSummary(
                                userId = it.user.id,
                                nickname = it.user.nickname,
                            )
                        },
                )
            }

        return AvailabilitySummaryResponse(
            teamId = team.id,
            roomType = team.roomType,
            slots = slots,
        )
    }

    private fun findTeam(teamId: Long): Team {
        return teamRepository.findById(teamId).orElseThrow {
            BusinessException(ErrorCode.TEAM_NOT_FOUND)
        }
    }

    private fun parseAndValidate(team: Team, request: AvailabilitySlotRequest): SlotKey {
        val slot = SlotKey(
            date = request.date?.let(LocalDate::parse),
            dayOfWeek = request.dayOfWeek?.let { DayOfWeek.valueOf(it.uppercase()) },
            startTime = LocalTime.parse(request.startTime),
            endTime = LocalTime.parse(request.endTime),
        )
        AvailabilityValidator.validate(
            roomType = team.roomType,
            date = slot.date,
            dayOfWeek = slot.dayOfWeek,
            startTime = slot.startTime,
            endTime = slot.endTime,
        )
        if (slot.date != null && (slot.date.isBefore(team.startDate) || slot.date.isAfter(team.endDate))) {
            throw BusinessException(ErrorCode.AVAILABILITY_OUTSIDE_TEAM_PERIOD)
        }
        return slot
    }

    private data class SlotKey(
        val date: LocalDate?,
        val dayOfWeek: DayOfWeek?,
        val startTime: LocalTime,
        val endTime: LocalTime,
    )
}
