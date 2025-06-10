package com.devse.club_platform_server.dto.request

import com.devse.club_platform_server.domain.MemberRole
import jakarta.validation.constraints.NotNull

// 멤버 역할 변경 요청
data class UpdateMemberRoleRequest(
    @field:NotNull(message = "새 역할은 필수입니다")
    val newRole: MemberRole
)

// 멤버 강퇴 요청 (필요한 경우)
data class RemoveMemberRequest(
    val reason: String? = null
)