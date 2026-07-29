# 프론트엔드 API 연동 변경사항

## 현재 상태

- 팀·일정·희망시간 API: **Railway 배포 완료**, Scalar 스모크 통과
- Base URL: `https://moijangbackend-production.up.railway.app`
- API 문서: `https://moijangbackend-production.up.railway.app/scalar`
- 로컬 Base URL: `http://localhost:8080` / 문서: `http://localhost:8080/scalar`
- 인증: 아직 `userId=1` 고정 (세션 OAuth는 인증 담당 작업 중). 지금은 로그인 없이 팀/일정 API 호출 가능
- 로그인 방식: **JWT가 아니라 세션(쿠키)** 으로 진행하기로 결정됨

## 공통 형식

- 날짜: `YYYY-MM-DD`
- 시간: `HH:mm`
- 요일: `MONDAY` ~ `SUNDAY`
- 방 유형: `SHORT_TERM`, `RECURRING`

## 중요 변경사항

### 1. 방 생성

`POST /api/v1/teams`

응답에 초대 코드와 초대 링크가 포함됩니다.

```json
{
  "data": {
    "teamId": 1,
    "inviteCode": "ABCD",
    "inviteLink": "http://localhost:5173/join?code=ABCD"
  },
  "message": "방이 성공적으로 생성되었습니다."
}
```

`inviteLink`의 프론트 도메인은 Railway 변수 `INVITE_BASE_URL`로 바꿀 수 있습니다.

### 2. 방 조회

`GET /api/v1/teams/{teamId}`

응답에 `inviteCode`, `inviteLink`, 참여자 목록이 포함됩니다.

### 3. 초대코드 가입

`POST /api/v1/teams/join?code={inviteCode}&password={password}`

- `password`가 필수로 변경됨
- 잘못된 비밀번호: `TEAM_PASSWORD_MISMATCH`

### 4. 초대코드 재발급

`POST /api/v1/teams/{teamId}/invite-code`

- 방장만 호출 가능
- 기존 코드는 더 이상 사용할 수 없음
- 새 `inviteCode`, `inviteLink` 반환

### 5. 팀 일정 병합

`GET /api/v1/schedules/teams/{teamId}/merged`

각 날짜 또는 요일에 바쁜 시간과 공통으로 비어 있는 시간을 함께 반환합니다.

```json
{
  "data": {
    "teamId": 1,
    "roomType": "SHORT_TERM",
    "mergedSchedules": [
      {
        "date": "2026-07-10",
        "dayOfWeek": "FRIDAY",
        "busyTimes": [
          {
            "startTime": "13:00",
            "endTime": "13:30",
            "busyUserCount": 2
          }
        ],
        "freeTimes": [
          {
            "startTime": "00:00",
            "endTime": "13:00"
          },
          {
            "startTime": "13:30",
            "endTime": "24:00"
          }
        ]
      }
    ]
  },
  "message": "Success"
}
```

- `busyTimes`: 30분 단위, 해당 시간에 일정이 있는 팀원 수 포함
- `freeTimes`: 아무 팀원도 일정이 없는 연속 구간
- 정기방은 월요일부터 일요일까지 7개 요일을 모두 반환

### 6. 개인 일정 월별 조회

`GET /api/v1/schedules?year=2026&month=7`

반복 일정을 해당 월의 실제 날짜로 펼쳐 반환합니다.

예를 들어 매주 월요일 일정은 7월의 모든 월요일에 각각 나타납니다.

주의:

- 펼쳐진 반복 일정들은 같은 `scheduleId`를 가짐
- 수정·삭제는 특정 날짜 한 건이 아니라 반복 일정 전체에 적용됨
- 따라서 이전보다 응답 배열의 크기가 커질 수 있음

### 7. 희망 시간

```text
PUT /api/v1/teams/{teamId}/availabilities
GET /api/v1/teams/{teamId}/availabilities
```

- `PUT`은 현재 사용자의 기존 희망 시간을 요청 배열 전체로 교체
- 단기방: `date` 사용
- 정기방: `dayOfWeek` 사용

## 아직 연동하지 않는 기능

- Google OAuth **세션** 로그인 (경로만 있음, 프론트·유저 DB 연동 미완)
- 로그아웃 (백엔드 엔드포인트는 있으나 로그인 완성 후 사용)
- 친구 추가·조회·삭제 (스텁/팀원 담당)
- 친구를 통한 방 초대
- 알림 조회·수락·거절

## 프론트에서 필요한 작업

1. API Base URL을 `https://moijangbackend-production.up.railway.app` 로 설정 (로컬 개발 시에는 `http://localhost:8080`)
2. 방 가입 화면에서 `code`와 `password`를 함께 전송
3. 병합 화면에서 `freeTimes`를 가능한 시간으로 표시
4. 반복 일정 항목에는 “반복 일정 전체 수정/삭제”임을 표시
5. 로그인 연동은 세션 쿠키 기준으로 인증 담당과 맞춘 뒤 진행 (`credentials: 'include'` 등)
