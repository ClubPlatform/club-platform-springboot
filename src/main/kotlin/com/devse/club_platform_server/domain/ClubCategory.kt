package com.devse.club_platform_server.domain

import jakarta.persistence.*

@Entity
@Table(name = "club_category")
data class ClubCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    val categoryId: Int = 0,

    @Column(name = "name", nullable = false, length = 100, unique = true)
    val name: String
)