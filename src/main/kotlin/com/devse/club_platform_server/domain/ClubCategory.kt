package com.devse.club_platform_server.domain

import jakarta.persistence.*

/*
동아리 카테고리 정보를 관리하는 엔티티
- 동아리 분류를 위한 카테고리 (학술, 취미, 운동 등)
- 동아리 등록 시 카테고리 선택을 위한 기준 데이터
 */

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