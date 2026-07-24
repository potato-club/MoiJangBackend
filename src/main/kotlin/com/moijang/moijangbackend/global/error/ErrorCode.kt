package com.moijang.moijangbackend.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 친구가 없습니다"),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다"),
    SCHEDULE_FORBIDDEN(HttpStatus.FORBIDDEN, "일정에 대한 권한이 없습니다"),
    SCHEDULE_OUTSIDE_TEAM_PERIOD(HttpStatus.BAD_REQUEST, "확정 날짜는 팀 일정 기간 안이어야 합니다"),
    AVAILABILITY_OUTSIDE_TEAM_PERIOD(HttpStatus.BAD_REQUEST, "희망 날짜는 팀 일정 기간 안이어야 합니다"),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다"),
    TEAM_FORBIDDEN(HttpStatus.FORBIDDEN, "팀에 대한 권한이 없습니다"),
    TEAM_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다"),
    TEAM_FULL(HttpStatus.BAD_REQUEST, "팀 정원이 가득 찼습니다"),
    ALREADY_TEAM_MEMBER(HttpStatus.BAD_REQUEST, "이미 참여 중인 팀입니다"),
    INVALID_TEAM_PERIOD(HttpStatus.BAD_REQUEST, "종료일은 시작일 이후여야 합니다"),
    CANNOT_KICK_LEADER(HttpStatus.BAD_REQUEST, "방장은 강퇴할 수 없습니다"),
}
