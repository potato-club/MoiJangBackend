package com.moijang.moijangbackend.global.auth

/**
 * [소유: 인증 담당] 구현 교체
 * [사용: 전 도메인] 컨트롤러에서 [id]만 호출
 *
 * OAuth/JWT 완료 후 SecurityContext의 AuthUser(또는 @AuthenticationPrincipal)로 교체한다.
 * 팀·일정·친구 담당자는 이 파일의 내부를 수정하지 않는다.
 */
object CurrentUser {
    // TODO(인증 담당): TEMP_USER_ID 제거 → 실제 로그인 사용자 ID 반환
    private const val TEMP_USER_ID = 1L

    fun id(): Long = TEMP_USER_ID
}
