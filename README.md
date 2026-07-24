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


| 담당                | 패키지 / 파일                                                                                    | 하는 일                                                   | 하지 말 것                                     |
| ----------------- | ------------------------------------------------------------------------------------------- | ------------------------------------------------------ | ------------------------------------------ |
| **인증 (팀원)**       | `config/SecurityConfig`, `global/auth/`*, `auth/`(신규), `application.yml`의 `security.oauth2` | OAuth 로그인, JWT 발급/검증, logout, `CurrentUser` 실연동, 인가 규칙 | team/schedule 비즈니스 로직                      |
| **유저·친구·알림 (팀원)** | `user/`, `friend/`, `notification/`(신규)                                                     | `/users/me`, 친구, 알림                                    | SecurityFilterChain 세부 구현                  |
| **팀·일정·희망시간 (나)** | `team/`, `schedule/`, `availability/`                                                       | 팀 CRUD, 일정 CRUD, merged/confirm, 희망시간                  | `SecurityConfig` / `CurrentUser` **내부** 수정 |
| **공통 (합의 후)**     | `global/common`, `global/error`, `config/PasswordEncoderConfig`                             | 응답 형식, ErrorCode 추가, 비밀번호 인코더                          | 임의로 응답 스키마 변경                              |




### 인증 범위 (팀원이 맡는 것 — CurrentUser·SecurityConfig만이 아님)

1. `SecurityConfig` — 필터 체인, OAuth2 로그인, URL 인가
2. `CurrentUser` **구현 교체** — JWT/SecurityContext에서 실제 userId 꺼내기
3. `application.yml`의 `spring.security.oauth2.client` — Google client id/secret
4. Auth API — `POST /api/v1/auth/logout`, OAuth 성공 시 리다이렉트(`?token=`)
5. JWT 유틸/필터, `AuthUser` / `OAuth2UserService` 등 (신규 파일)

**팀·일정 담당자(나)가 하는 인증 관련 작업은 하나뿐:**  
컨트롤러에서 `CurrentUser.id()`를 **호출**만 한다. 구현·설정은 건드리지 않는다.

예외(프론트 연동): `SecurityConfig`에 `http.cors { }` 한 줄만 추가했고, origin 목록은 `CorsConfig` / `app.cors.allowed-origins`에서 관리한다.

팀방 비밀번호 해시는 `PasswordEncoderConfig`에 두었으므로, 팀 기능 때문에 `SecurityConfig`를 열 필요가 없다.

### ErrorCode 규칙

- 자기 도메인 prefix만 추가: `TEAM_*`, `SCHEDULE_*`, `FRIEND_*`, `AUTH_*` …
- 기존 enum 값의 의미/이름을 바꾸지 않기



## API 엔드포인트



### 로그인 및 유저


| 메서드    | 경로                             | 설명          | 구현       |
| ------ | ------------------------------ | ----------- | -------- |
| `GET`  | `/oauth2/authorization/google` | 구글 OAuth 진입 | 예정(인증)   |
| `POST` | `/api/v1/auth/logout`          | 로그아웃        | 예정(인증)   |
| `GET`  | `/api/v1/users/me`             | 내 프로필       | stub(유저) |




### 친구


| 메서드      | 경로                           | 설명    | 구현       |
| -------- | ---------------------------- | ----- | -------- |
| `POST`   | `/api/v1/friends?email=`     | 친구 요청 | stub(친구) |
| `GET`    | `/api/v1/friends`            | 목록    | stub(친구) |
| `DELETE` | `/api/v1/friends/{friendId}` | 삭제    | stub(친구) |




### 팀


| 메서드      | 경로                                     | 설명                    | 구현  |
| -------- | -------------------------------------- | --------------------- | --- |
| `POST`   | `/api/v1/teams`                        | 개설 (`inviteCode`+`inviteLink`) | O   |
| `GET`    | `/api/v1/teams/{teamId}`               | 상세 (초대코드/링크 포함)       | O   |
| `DELETE` | `/api/v1/teams/{teamId}`               | 삭제                    | O   |
| `POST`   | `/api/v1/teams/join?code=&password=`   | 초대코드+비밀번호 가입          | O   |
| `POST`   | `/api/v1/teams/{teamId}/invite-code`   | 초대코드 재발급 (방장)         | O   |
| `DELETE` | `/api/v1/teams/{teamId}/kick/{userId}` | 강퇴                    | O   |




### 일정


| 메서드      | 경로                                         | 설명                              | 구현  |
| -------- | ------------------------------------------ | ------------------------------- | --- |
| `POST`   | `/api/v1/schedules`                        | 등록                              | O   |
| `GET`    | `/api/v1/schedules?year=&month=`           | 월별 조회 (반복 일정은 해당 월 날짜로 펼침)     | O   |
| `PUT`    | `/api/v1/schedules/{id}`                   | 수정                              | O   |
| `DELETE` | `/api/v1/schedules/{id}`                   | 삭제                              | O   |
| `GET`    | `/api/v1/schedules/teams/{teamId}/merged`  | 병합 (`busyTimes`+`freeTimes`)   | O   |
| `POST`   | `/api/v1/schedules/teams/{teamId}/confirm` | 약속 확정                           | O   |




### 희망 시간


| 메서드   | 경로                                      | 설명            | 구현  |
| ----- | --------------------------------------- | ------------- | --- |
| `PUT` | `/api/v1/teams/{teamId}/availabilities` | 내 희망 시간 전체 교체 | O   |
| `GET` | `/api/v1/teams/{teamId}/availabilities` | 팀 희망 시간 요약    | O   |




### 알림


| 메서드    | 경로                                  | 설명  | 구현     |
| ------ | ----------------------------------- | --- | ------ |
| `GET`  | `/api/v1/notifications`             | 목록  | 예정(알림) |
| `POST` | `/api/v1/notifications/{id}/accept` | 수락  | 예정(알림) |
| `POST` | `/api/v1/notifications/{id}/reject` | 거절  | 예정(알림) |




## 로컬 실행

```powershell
.\gradlew.bat bootRun
```

- API: `http://localhost:8080`
- 문서: `http://localhost:8080/scalar`
- 기본 DB: H2 인메모리
- 시드 유저: `test-user@gmail.com` (CurrentUser는 아직 `userId=1` 고정)



## Dev 배포 (Railway + MySQL)

프론트가 공용 API에 붙도록 **Railway에서 MySQL + 백엔드 앱**을 같이 올린다.

### 코드 쪽 (이미 준비됨)


| 항목   | 내용                                                   |
| ---- | ---------------------------------------------------- |
| 프로필  | `application-dev.yml` (`SPRING_PROFILES_ACTIVE=dev`) |
| DB   | MySQL (Railway `MYSQL*` 변수 자동 연동)                    |
| 포트   | Railway `PORT` → `server.port`                       |
| CORS | `CorsConfig` + `http.cors { }` (`5173`, `3000`)      |
| 배포   | `Dockerfile`, `railway.toml`                         |




### 1. Railway 프로젝트 만들기

1. [railway.app](https://railway.app) 가입 (GitHub 로그인 추천)
2. **New Project**
3. GitHub repo `MoiJangBackend` 연결



### 2. MySQL 추가

1. 프로젝트에서 **+ New → Database → MySQL**
2. MySQL 서비스가 생성되면 끝 (별도 URL 복사는 아직 불필요)
3. 나중에 앱 서비스와 **연결(Connect / Variable Reference)** 하면 `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`가 앱에 주입된다



### 3. 백엔드 앱 서비스 추가

1. **+ New → GitHub Repo** (같은 repo) 또는 Empty Service 후 repo 연결
2. Settings → **Build**
  - Builder: Dockerfile (`railway.toml`에 이미 지정)
3. Settings → **Networking → Generate Domain**
  - 예: `https://moijang-backend-production.up.railway.app`
4. MySQL ↔ 앱 **연결**
  - 앱 서비스 Variables에서 MySQL 변수를 Reference로 추가
  - 또는 Railway UI의 **Connect** / service link 사용



### 4. 앱 Environment Variables

앱 서비스 Variables에 최소 아래를 넣는다.

```text
SPRING_PROFILES_ACTIVE=dev
GOOGLE_CLIENT_ID=placeholder
GOOGLE_CLIENT_SECRET=placeholder
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```
MySQL은 서비스 연결만 되어 있으면 아래가 자동으로 들어온다.

```text
MYSQLHOST
MYSQLPORT
MYSQLUSER
MYSQLPASSWORD
MYSQLDATABASE
```

수동으로 넣을 때만 사용:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://HOST:3306/DBNAME?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```



### 5. Deploy & 확인

1. Deploy 로그에서 `Started MoiJangBackendApplication` 확인
2. 브라우저에서 확인
  - `https://<railway-domain>/scalar`
  - `https://<railway-domain>/api/v1/users/me`

시드 유저(`test-user@gmail.com`)가 자동 생성되고, 현재는 `userId=1`로 API가 동작한다.

### 6. 팀에 공유

```text
Base URL: https://<railway-domain>
문서:     https://<railway-domain>/scalar
주의:     인증 미완, userId=1 고정
CORS:     localhost:5173, localhost:3000 허용
```

- **인증 담당**: Google OAuth redirect URI / client id·secret을 Railway Variables에 넣어 주세요.
- **프론트**: Base URL을 `.env`에 넣고 연동 테스트해 주세요.



### 로컬에서 Docker로 미리 확인 (선택)

MySQL이 로컬/원격에 있을 때:

```powershell
docker build -t moijang-backend .
docker run -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=dev `
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/moijang?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul `
  -e SPRING_DATASOURCE_USERNAME=root `
  -e SPRING_DATASOURCE_PASSWORD=password `
  moijang-backend
```

