package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.*
import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PostService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val scrapRepository: ScrapRepository,
    private val userRepository: UserRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val boardService: BoardService
) {

    private val logger = LoggerFactory.getLogger(PostService::class.java)

    // 게시글 작성
    fun createPost(request: CreatePostRequest, userId: Long): CreatePostResponse {
        logger.info("게시글 작성 요청: boardId=${request.boardId}, userId=$userId, isAnonymous=${request.isAnonymous}")

        // 게시판 접근 권한 확인
        val clubId = boardService.validateBoardAccess(request.boardId, userId)

        // 공지사항 작성 권한 확인
        if (request.isNotice == true) {
            val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
            if (membership?.role != MemberRole.owner && membership?.role != MemberRole.staff) {
                throw IllegalArgumentException("공지사항은 관리자 이상만 작성할 수 있습니다.")
            }
        }

        val post = Post(
            boardId = request.boardId,
            authorId = userId,
            title = request.title,
            content = request.content,
            isNotice = request.isNotice,
            isAnonymous = request.isAnonymous
        )

        val savedPost = postRepository.save(post)

        logger.info("게시글 작성 완료: postId=${savedPost.postId}")

        return CreatePostResponse(
            success = true,
            message = "게시글이 성공적으로 작성되었습니다.",
            postId = savedPost.postId,
            createdAt = savedPost.createdAt
        )
    }

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    fun getPostList(boardId: Long, boardType: String, userId: Long): PostListResponse {
        logger.info("게시글 목록 조회: boardId=$boardId, type=$boardType, userId=$userId")

        val posts = when (boardType.lowercase()) {
            "general", "notice", "tips" -> {
                // 일반 게시판별 조회
                boardService.validateBoardAccess(boardId, userId)
                postRepository.findByBoardIdOrderByNoticeAndCreatedAt(boardId)
            }
            "hot" -> {
                // HOT 게시글 (일주일 내, 상호작용 20개 이상)
                boardService.validateBoardAccess(boardId, userId)
                val weekAgo = LocalDateTime.now().minusWeeks(1)
                postRepository.findHotPosts(boardId, weekAgo)
            }
            "best" -> {
                // BEST 게시글 (한달 내, 상호작용 50개 이상)
                boardService.validateBoardAccess(boardId, userId)
                val monthAgo = LocalDateTime.now().minusMonths(1)
                postRepository.findBestPosts(boardId, monthAgo)
            }
            "my_posts" -> {
                // 내가 작성한 글
                postRepository.findByAuthorIdOrderByCreatedAtDesc(userId)
            }
            "my_comments" -> {
                // 내가 댓글을 단 글
                postRepository.findPostsWithMyComments(userId)
            }
            "my_scraps" -> {
                // 내가 스크랩한 글
                postRepository.findMyScrapedPosts(userId)
            }
            else -> {
                throw IllegalArgumentException("지원하지 않는 게시판 타입입니다: $boardType")
            }
        }

        val postListItems = posts.map { post ->
            val author = userRepository.findById(post.authorId).orElse(null)
            PostListItem(
                postId = post.postId,
                title = post.title,
                content = post.content,
                authorName = if (post.isAnonymous) "익명" else (author?.name ?: "알 수 없음"),
                createdAt = post.createdAt,
                viewCount = post.viewCount,
                commentCount = post.commentCount
            )
        }

        logger.info("게시글 목록 조회 완료: 게시글수=${postListItems.size}")

        return PostListResponse(
            success = true,
            message = "게시글 목록 조회 성공",
            posts = postListItems
        )
    }

    // 게시글 상세 조회
    @Transactional
    fun getPostDetail(postId: Long, userId: Long): PostDetailResponse {
        logger.info("게시글 상세 조회: postId=$postId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        // 게시판 접근 권한 확인
        val clubId = boardService.validateBoardAccess(post.boardId, userId)

        // 조회수 증가
        postRepository.incrementViewCount(postId)

        // 사용자별 상태 조회
        val isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId)
        val isScraped = scrapRepository.existsByUserIdAndPostId(userId, postId)

        // 수정/삭제 권한 확인
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
        val canEdit = post.authorId == userId || 
                     (membership?.role == MemberRole.owner || membership?.role == MemberRole.staff)
        val canDelete = canEdit

        val author = userRepository.findById(post.authorId).orElse(null)

        val postDetail = PostDetailInfo(
            postId = post.postId,
            title = post.title,
            content = post.content,
            authorName = if (post.isAnonymous) "익명" else (author?.name ?: "알 수 없음"),
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
            viewCount = post.viewCount + 1, // 증가된 조회수 반영
            likeCount = post.likeCount,
            isLiked = isLiked,
            isScraped = isScraped,
            isAnonymous = post.isAnonymous,
            canEdit = canEdit,
            canDelete = canDelete
        )

        logger.info("게시글 상세 조회 완료: postId=$postId")

        return PostDetailResponse(
            success = true,
            message = "게시글 조회 성공",
            post = postDetail
        )
    }

    // 게시글 수정
    fun updatePost(postId: Long, request: UpdatePostRequest, userId: Long): UpdatePostResponse {
        logger.info("게시글 수정 요청: postId=$postId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        // 게시판 접근 권한 확인
        val clubId = boardService.validateBoardAccess(post.boardId, userId)

        // 수정 권한 확인
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
        val canEdit = post.authorId == userId || 
                     (membership?.role == MemberRole.owner || membership?.role == MemberRole.staff)
        
        if (!canEdit) {
            throw IllegalArgumentException("게시글을 수정할 권한이 없습니다.")
        }

        // 공지사항 설정 권한 확인
        if (request.isNotice == true && post.authorId != userId) {
            if (membership?.role != MemberRole.owner && membership?.role != MemberRole.staff) {
                throw IllegalArgumentException("공지사항 설정은 관리자 이상만 가능합니다.")
            }
        }

        val currentTime = LocalDateTime.now()
        val updatedPost = post.copy(
            title = request.title ?: post.title,
            content = request.content ?: post.content,
            isNotice = request.isNotice ?: post.isNotice,
            updatedAt = currentTime
        )

        postRepository.save(updatedPost)

        logger.info("게시글 수정 완료: postId=$postId")

        return UpdatePostResponse(
            success = true,
            message = "게시글이 성공적으로 수정되었습니다.",
            updatedAt = currentTime
        )
    }

    // 게시글 삭제
    fun deletePost(postId: Long, userId: Long) {
        logger.info("게시글 삭제 요청: postId=$postId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        // 게시판 접근 권한 확인
        val clubId = boardService.validateBoardAccess(post.boardId, userId)

        // 삭제 권한 확인
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
        val canDelete = post.authorId == userId || 
                       (membership?.role == MemberRole.owner || membership?.role == MemberRole.staff)
        
        if (!canDelete) {
            throw IllegalArgumentException("게시글을 삭제할 권한이 없습니다.")
        }

        postRepository.delete(post)

        logger.info("게시글 삭제 완료: postId=$postId")
    }

    // 게시글 좋아요 토글
    fun togglePostLike(postId: Long, userId: Long): LikeToggleResponse {
        logger.info("게시글 좋아요 토글: postId=$postId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        // 게시판 접근 권한 확인
        boardService.validateBoardAccess(post.boardId, userId)

        val existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId)

        val newIsLiked = if (existingLike == null) {
            // 좋아요가 없으면 추가
            val postLike = PostLike(postId = postId, userId = userId)
            postLikeRepository.save(postLike)
            true
        } else {
            // 좋아요가 있으면 제거
            postLikeRepository.delete(existingLike)
            false
        }

        // 좋아요 수 업데이트
        val newLikeCount = postLikeRepository.countByPostId(postId).toInt()
        postRepository.updateLikeCount(postId, newLikeCount)

        logger.info("게시글 좋아요 토글 완료: postId=$postId, newIsLiked=$newIsLiked, likeCount=$newLikeCount")

        return LikeToggleResponse(
            success = true,
            message = if (newIsLiked) "좋아요를 눌렀습니다." else "좋아요를 취소했습니다.",
            isLiked = newIsLiked,
            likeCount = newLikeCount
        )
    }

    // 게시글 스크랩 토글
    fun togglePostScrap(postId: Long, userId: Long): ScrapToggleResponse {
        logger.info("게시글 스크랩 토글: postId=$postId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        // 게시판 접근 권한 확인
        boardService.validateBoardAccess(post.boardId, userId)

        val existingScrap = scrapRepository.findByUserIdAndPostId(userId, postId)

        val newIsScraped = if (existingScrap == null) {
            // 스크랩 추가
            val scrap = Scrap(userId = userId, postId = postId)
            scrapRepository.save(scrap)
            true
        } else {
            // 스크랩 취소
            scrapRepository.delete(existingScrap)
            false
        }

        logger.info("게시글 스크랩 토글 완료: postId=$postId, newIsScraped=$newIsScraped")

        return ScrapToggleResponse(
            success = true,
            message = if (newIsScraped) "스크랩했습니다." else "스크랩을 취소했습니다.",
            isScraped = newIsScraped
        )
    }
}