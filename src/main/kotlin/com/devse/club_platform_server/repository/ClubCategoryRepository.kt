package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.ClubCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ClubCategoryRepository : JpaRepository<ClubCategory, Int> {
    fun findByName(name: String): Optional<ClubCategory>
    fun findByNameIgnoreCase(name: String): Optional<ClubCategory>
}