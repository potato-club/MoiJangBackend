# 모이장 백엔드

> 모이장은 소모임을 위한 간편한 일정 조율 서비스입니다.

## 표준 응답 모델

```json
Success<T> { "data": T, "message": "String" }
Ok        { "message": "String" }
Failure   { "errorCode": "String", "errorMessage": "String" }
```

- 데이터 있는 성공 → `Success` (`message`는 응답 최상위, data 안에 중복하지 않음)
- 데이터 없는 성공(수정/삭제 등) → `Ok`
- 실패 → `Failure` (`BusinessException` + `ErrorCode` + `GlobalExceptionHandler`)
- 컨트롤러에서 `ApiResponse.Failure`를 직접 반환하지 않는다

## 도메인 담당 (병합 충돌 줄이기)

| 담당 | 패키지 / 파일 | 하는 일 | 하지 말 것 |
|:---|:---|:---|:---|
| **인증 (팀원)** | `config/SecurityConfig`, `global/auth/*`, `auth/`(신규), `application.yml`의 `security.oauth2` | OAuth 로그인, JWT 발급/검증, logout, `CurrentUser` 실연동, 인가 규칙 | team/schedule 비즈니스 로직 |
| **유저·친구·알림 (팀원)** | `user/`, `friend/`, `notification/`(신규) | `/users/me`, 친구, 알림 | SecurityFilterChain 세부 구현 |
| **팀·일정·희망시간 (나)** | `team/`, `schedule/`, `availability/` | 팀 CRUD, 일정 CRUD, merged/confirm, 희망시간 | `SecurityConfig` / `CurrentUser` **내부** 수정 |
| **공통 (합의 후)** | `global/common`, `global/error`, `config/PasswordEncoderConfig` | 응답 형식, ErrorCode 추가, 비밀번호 인코더 | 임의로 응답 스키마 변경 |

### 인증 범위 (팀원이 맡는 것 — CurrentUser·SecurityConfig만이 아님)

1. `SecurityConfig` — 필터 체인, OAuth2 로그인, URL 인가
2. `CurrentUser` **구현 교체** — JWT/SecurityContext에서 실제 userId 꺼내기
3. `application.yml`의 `spring.security.oauth2.client` — Google client id/secret
4. Auth API — `POST /api/v1/auth/logout`, OAuth 성공 시 리다이렉트(`?token=`)
5. JWT 유틸/필터, `AuthUser` / `OAuth2UserService` 등 (신규 파일)

**팀·일정 담당자(나)가 하는 인증 관련 작업은 하나뿐:**  
컨트롤러에서 `CurrentUser.id()`를 **호출**만 한다. 구현·설정은 건드리지 않는다.

팀방 비밀번호 해시는 `PasswordEncoderConfig`에 두었으므로, 팀 기능 때문에 `SecurityConfig`를 열 필요가 없다.

### ErrorCode 규칙

- 자기 도메인 prefix만 추가: `TEAM_*`, `SCHEDULE_*`, `FRIEND_*`, `AUTH_*` …
- 기존 enum 값의 의미/이름을 바꾸지 않기

## API 엔드포인트

### 로그인 및 유저

| 메서드 | 경로 | 설명 | 구현 |
|:---:|:---|:---|:---:|
| `GET` | `/oauth2/authorization/google` | 구글 OAuth 진입 | 예정(인증) |
| `POST` | `/api/v1/auth/logout` | 로그아웃 | 예정(인증) |
| `GET` | `/api/v1/users/me` | 내 프로필 | stub(유저) |

### 친구

| 메서드 | 경로 | 설명 | 구현 |
|:---:|:---|:---|:---:|
| `POST` | `/api/v1/friends?email=` | 친구 요청 | stub(친구) |
| `GET` | `/api/v1/friends` | 목록 | stub(친구) |
| `DELETE` | `/api/v1/friends/{friendId}` | 삭제 | stub(친구) |

### 팀

| 메서드 | 경로 | 설명 | 구현 |
|:---:|:---|:---|:---:|
| `POST` | `/api/v1/teams` | 개설 | O |
| `GET` | `/api/v1/teams/{teamId}` | 상세 | O |
| `DELETE` | `/api/v1/teams/{teamId}` | 삭제 | O |
| `POST` | `/api/v1/teams/join?code=` | 초대코드 가입 | O |
| `DELETE` | `/api/v1/teams/{teamId}/kick/{userId}` | 강퇴 | O |

### 일정

| 메서드 | 경로 | 설명 | 구현 |
|:---:|:---|:---|:---:|
| `POST` | `/api/v1/schedules` | 등록 | O |
| `GET` | `/api/v1/schedules?year=&month=` | 월별 조회 (`data`는 배열) | O |
| `PUT` | `/api/v1/schedules/{id}` | 수정 | O |
| `DELETE` | `/api/v1/schedules/{id}` | 삭제 | O |
| `GET` | `/api/v1/schedules/teams/{teamId}/merged` | 불가능 시간대 | 예정(일정) |
| `POST` | `/api/v1/schedules/teams/{teamId}/confirm` | 약속 확정 | 예정(일정) |

### 알림

| 메서드 | 경로 | 설명 | 구현 |
|:---:|:---|:---|:---:|
| `GET` | `/api/v1/notifications` | 목록 | 예정(알림) |
| `POST` | `/api/v1/notifications/{id}/accept` | 수락 | 예정(알림) |
| `POST` | `/api/v1/notifications/{id}/reject` | 거절 | 예정(알림) |
