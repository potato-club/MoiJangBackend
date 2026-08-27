package com.moijang.moijangbackend.friend.entity

import com.moijang.moijangbackend.user.entity.User
import jakarta.persistence.*

@Entity
@Table(name = "friends")
class Friend(
    @ManyToOne(fetch = FetchType.LAZY)
    var friend: User,

    @ManyToOne(fetch = FetchType.LAZY)
    var user: User,

    @Column(nullable = false)
    var createdAt: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}