package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean

    fun existsByStudentId(studentId: String): Boolean
}