package com.moijang.moijangbackend

import com.fasterxml.jackson.databind.ObjectMapper
import com.moijang.moijangbackend.schedule.repository.PersonalScheduleRepository
import com.moijang.moijangbackend.team.repository.TeamRepository
import com.moijang.moijangbackend.team.repository.TeamUserRepository
import com.moijang.moijangbackend.user.entity.OAuthProvider
import com.moijang.moijangbackend.user.entity.User
import com.moijang.moijangbackend.user.entity.UserRole
import com.moijang.moijangbackend.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@Transactional
class BackendBFlowIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private val objectMapper = ObjectMapper()

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var teamRepository: TeamRepository

    @Autowired
    private lateinit var teamUserRepository: TeamUserRepository

    @Autowired
    private lateinit var personalScheduleRepository: PersonalScheduleRepository

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        teamUserRepository.deleteAll()
        personalScheduleRepository.deleteAll()
        teamRepository.deleteAll()

        if (userRepository.findByEmail("test-user@gmail.com") == null) {
            userRepository.save(
                User(
                    email = "test-user@gmail.com",
                    nickname = "하영",
                    provider = OAuthProvider.GOOGLE,
                    providerId = "dev-test-user",
                    role = UserRole.USER,
                ),
            )
        }
    }

    @Test
    fun `팀 생성 조회 삭제 API 흐름`() {
        val createBody = """
            {
              "title": "모이장 회의",
              "roomType": "SHORT_TERM",
              "maxParticipants": 10,
              "isPublic": false,
              "password": "potato123",
              "startDate": "2026-07-01",
              "endDate": "2026-07-31"
            }
        """.trimIndent()

        val createResponse = mockMvc.perform(
            post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.teamId").exists())
            .andExpect(jsonPath("$.data.inviteCode").exists())
            .andExpect(jsonPath("$.message").value("방이 성공적으로 생성되었습니다."))
            .andReturn()

        val teamId = objectMapper.readTree(createResponse.response.contentAsString)
            .path("data")
            .path("teamId")
            .asLong()

        mockMvc.perform(get("/api/v1/teams/$teamId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.title").value("모이장 회의"))
            .andExpect(jsonPath("$.data.roomType").value("SHORT_TERM"))
            .andExpect(jsonPath("$.data.maxParticipants").value(10))
            .andExpect(jsonPath("$.data.participants").isArray)
            .andExpect(jsonPath("$.data.leaderId").value(1))

        mockMvc.perform(delete("/api/v1/teams/$teamId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("방 삭제 완료"))
    }

    @Test
    fun `일정 CRUD API 흐름`() {
        val createBody = """
            {
              "title": "감자볶음밥 회의",
              "categoryColor": "#FF0000",
              "isRepeating": false,
              "date": "2026-07-10",
              "startTime": "13:00",
              "endTime": "14:00"
            }
        """.trimIndent()

        val createResponse = mockMvc.perform(
            post("/api/v1/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.scheduleId").exists())
            .andExpect(jsonPath("$.message").value("일정이 성공적으로 등록되었습니다."))
            .andReturn()

        val scheduleId = objectMapper.readTree(createResponse.response.contentAsString)
            .path("data")
            .path("scheduleId")
            .asLong()

        mockMvc.perform(get("/api/v1/schedules?year=2026&month=7"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].title").value("감자볶음밥 회의"))
            .andExpect(jsonPath("$.data[0].isRepeating").value(false))

        val updateBody = """
            {
              "title": "감자전 부치는 날",
              "categoryColor": "#FF0000",
              "isRepeating": false,
              "date": "2026-07-10",
              "startTime": "15:00",
              "endTime": "16:00"
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/v1/schedules/$scheduleId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("일정이 성공적으로 수정되었습니다."))

        mockMvc.perform(delete("/api/v1/schedules/$scheduleId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("일정이 성공적으로 삭제되었습니다."))
    }

    @Test
    fun `팀 생성 후 일정 등록까지 연속 흐름`() {
        mockMvc.perform(
            post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "모이장 1차",
                      "roomType": "SHORT_TERM",
                      "maxParticipants": 5,
                      "isPublic": false,
                      "password": "potato123",
                      "startDate": "2026-07-01",
                      "endDate": "2026-07-31"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.inviteCode").exists())

        mockMvc.perform(
            post("/api/v1/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "킥오프 미팅",
                      "categoryColor": "#00FF00",
                      "isRepeating": false,
                      "date": "2026-07-05",
                      "startTime": "18:00",
                      "endTime": "19:00"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.scheduleId").exists())

        mockMvc.perform(get("/api/v1/schedules?year=2026&month=7"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].title").value("킥오프 미팅"))
    }

    @Test
    fun `존재하지 않는 팀 조회 시 Failure를 반환한다`() {
        mockMvc.perform(get("/api/v1/teams/99999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.errorCode").value("TEAM_NOT_FOUND"))
            .andExpect(jsonPath("$.errorMessage").exists())
    }
}
