package com.moijang.moijangbackend.friend.entity

import jakarta.persistence.*

@Entity
@Table(name = "friends")
class Friend(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

}