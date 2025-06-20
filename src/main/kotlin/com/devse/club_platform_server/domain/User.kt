package com.devse.club_platform_server.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/*
사용자 정보를 관리하는 엔티티
- 회원가입, 로그인 시 사용자 기본 정보 (이메일, 비밀번호) 저장
- 대학교, 학과, 전공, 학번 등 학생 신분 정보 포함
- 프로필 이미지 및 마지막 로그인 시간 추적
 */

@Entity
@Table(name = "user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,

    @Column(nullable = false, unique = true, length = 100)
    val email: String,

    @Column(nullable = false, length = 255)
    var password: String,

    @Column(nullable = false, length = 50)
    val name: String,

    @Column(nullable = false, length = 100)
    val university: String,

    @Column(nullable = false, length = 100)
    var department: String,

    @Column(nullable = false, length = 100)
    var major: String,

    @Column(name = "student_id", nullable = false, length = 20)
    val studentId: String,

    @Column(name = "profile_image", length = 255)
    var profileImage: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = LocalDateTime.now(),

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null
)