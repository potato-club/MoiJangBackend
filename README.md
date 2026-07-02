# 🥔📅 모이장 백엔드

> 모이장은 소모임을 위한 간편한 일정 조율 서비스입니다.

- ⚡ 복잡한 가입 절차가 필요 없습니다. 구글 계정으로 즉시 시작하세요!
- 💌 초대 링크를 사용하여 쉽게 초대하세요!
- ⏰ 한 번 일정을 등록하면 다시 등록할 필요가 없습니다!
- ✅ 모이장은 민감한 개인 정보를 저장하지 않습니다.

## API 엔드포인트

### 🔐 로그인 및 유저 API

|  메서드   | 경로                             | 설명       | 로그인 |
|:------:|:-------------------------------|:---------|:---:|
| `GET`  | `/oauth2/authorization/google` | 로그인 페이지  |  X  |
| `POST` | `/api/v1/auth/signout`         | 로그아웃     |  O  |
| `GET`  | `/api/v1/users/me`             | 내 프로필 조회 |  O  |

### 👥 친구 관리 API

|   메서드    | 경로                            | 설명       | 로그인 |
|:--------:|:------------------------------|:---------|:---:|
|  `POST`  | `/api/v1/friends?email={이메일}` | 친구 요청    |  O  |
|  `GET`   | `/api/v1/friends`             | 친구 목록 조회 |  O  |
| `DELETE` | `/api/v1/friends/{friendId}`  | 친구 삭제    |  O  |

### 🏠 팀 API

|   메서드    | 경로                                     | 설명               | 로그인 |
|:--------:|:---------------------------------------|:-----------------|:---:|
|  `POST`  | `/api/v1/teams`                        | 팀 개설             |  O  |
|  `GET`   | `/api/v1/teams/{teamId}`               | 팀 상세 정보 및 참여자 조회 |  O  |
| `DELETE` | `/api/v1/teams/{teamId}`               | 팀 삭제             |  O  |
|  `POST`  | `/api/v1/teams/join?code={초대코드}`       | 초대 코드로 팀 가입      |  O  |
| `DELETE` | `/api/v1/teams/{teamId}/kick/{userId}` | 팀원 강퇴            |  O  |

### 📅 일정 조율 API

|   메서드    | 경로                                         | 설명                    | 로그인 |
|:--------:|:-------------------------------------------|:----------------------|:---:|
|  `POST`  | `/api/v1/schedules`                        | 개인 일정 또는 고정 루틴 등록     |  O  |
|  `GET`   | `/api/v1/schedules`                        | 월별 내 모든 캘린더 일정 조회     |  O  |
|  `PUT`   | `/api/v1/schedules/{id}`                   | 등록된 개인 일정 수정          |  O  |
| `DELETE` | `/api/v1/schedules/{id}`                   | 등록된 개인 일정 삭제          |  O  |
|  `GET`   | `/api/v1/schedules/teams/{teamId}/merged`  | 참여자들의 겹치는 불가능한 시간대 추출 |  O  |
|  `POST`  | `/api/v1/schedules/teams/{teamId}/confirm` | 최종 약속 확정 및 팀원 캘린더 반영  |  O  |

### 🔔 알림 API

|  메서드   | 경로                                              | 설명               | 로그인 |
|:------:|:------------------------------------------------|:-----------------|:---:|
| `GET`  | `/api/v1/notifications`                         | 알림 목록 조회 (최신순)   |  O  |
| `POST` | `/api/v1/notifications/{notificationId}/accept` | 초대/친구 요청 수락      |  O  |
| `POST` | `/api/v1/notifications/{notificationId}/reject` | 초대/친구 요청 거절 및 삭제 |  O  |
