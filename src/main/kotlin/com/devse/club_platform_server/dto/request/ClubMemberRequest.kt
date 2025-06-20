package com.devse.club_platform_server.dto.request

import com.devse.club_platform_server.domain.MemberRole
import jakarta.validation.constraints.NotNull

/*
동아리 멤버 역할 변경 요청 DTO
- 동아리 소유자/관리자가 멤버의 권한을 변경할 때 사용
- 일반회원 ↔ 관리자 ↔ 소유자 간 역할 전환 처리
 */
data class UpdateMemberRoleRequest(
    @field:NotNull(message = "새 역할은 필수입니다")
    val newRole: MemberRole
)