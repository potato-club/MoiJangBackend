package com.moijang.moijangbackend.schedule.entity

import com.moijang.moijangbackend.team.entity.Team
import com.moijang.moijangbackend.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "personal_schedules")
class PersonalSchedule(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var categoryColor: String,

    @Column(nullable = false)
    var isRepeating: Boolean,

    var date: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    var dayOfWeek: DayOfWeek? = null,

    @Column(nullable = false)
    var startTime: LocalTime,

    @Column(nullable = false)
    var endTime: LocalTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_team_id")
    val sourceTeam: Team? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    fun updateContent(
        title: String,
        categoryColor: String,
        isRepeating: Boolean,
        date: LocalDate?,
        dayOfWeek: DayOfWeek?,
        startTime: LocalTime,
        endTime: LocalTime,
    ) {
        this.title = title
        this.categoryColor = categoryColor
        this.isRepeating = isRepeating
        this.date = date
        this.dayOfWeek = dayOfWeek
        this.startTime = startTime
        this.endTime = endTime
    }
}
