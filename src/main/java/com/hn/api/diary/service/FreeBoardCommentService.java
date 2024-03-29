package com.hn.api.diary.service;

import com.hn.api.diary.dto.freeBoard.FreeBoardCommentReplyDTO;
import com.hn.api.diary.dto.freeBoard.FreeBoardCommentUpdateDTO;
import com.hn.api.diary.dto.freeBoard.FreeBoardCommentWriteDTO;
import com.hn.api.diary.dto.freeBoard.FreeBoardCommentsDTO;
import com.hn.api.diary.entity.FreeBoardComment;
import com.hn.api.diary.entity.FreeBoardPost;
import com.hn.api.diary.entity.Member;
import com.hn.api.diary.exception.Forbidden;
import com.hn.api.diary.exception.InvalidValue;
import com.hn.api.diary.repository.FreeBoardCommentRepository;
import com.hn.api.diary.repository.FreeBoardPostRepository;
import com.hn.api.diary.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
public class FreeBoardCommentService {

    private final FreeBoardCommentRepository freeBoardCommentRepository;
    private final FreeBoardPostRepository freeBoardPostRepository;
    private final MemberRepository memberRepository;

    private class CommentPageSize {
        private static final int BASIC = 10;
    }

    public void delete(String commentId, String memberId) {
        FreeBoardComment freeBoardComment = freeBoardCommentRepository.findByIdWithNotDelete(Long.parseLong(commentId));

        // 코멘트 원글이고,
        if (freeBoardComment.isParent()) {
            // 답글이 있을 때 삭제할 수 없다. 그룹에 삭제하려는 코멘트 외에 더 존재하면 삭제할 수 없다.
            long countReplies = freeBoardCommentRepository.countByGroupIdWithNoDelete(freeBoardComment.getGroupId());
            if (countReplies > 1) throw new Forbidden();
        }

        // 코멘트 작성자가 아니면 삭제 불가.
        if(freeBoardComment.getMember().getId() != Long.parseLong(memberId)) throw new Forbidden();

        freeBoardComment.setDelete(true);
        freeBoardCommentRepository.save(freeBoardComment);
    }

    public void update(FreeBoardCommentUpdateDTO freeBoardCommentUpdateDTO, String memberId) {
        FreeBoardComment freeBoardComment = freeBoardCommentRepository.findByIdWithNotDelete(Long.parseLong(freeBoardCommentUpdateDTO.getCommentId()));
        if (freeBoardComment == null) throw new InvalidValue();

        if( freeBoardComment.getMember().getId() != Long.parseLong(memberId) ) throw new Forbidden();

        freeBoardComment.setContent(freeBoardCommentUpdateDTO.getContent());
        freeBoardCommentRepository.save(freeBoardComment);
    }

    public void write(FreeBoardCommentWriteDTO freeBoardCommentWriteDTO, String memberId) {
        Member member = memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(InvalidValue::new);

        FreeBoardPost freeBoardPost = freeBoardPostRepository.findByIdWithNotDelete(Long.parseLong(freeBoardCommentWriteDTO.getPostId()));
        if (freeBoardPost == null) throw new InvalidValue();

        FreeBoardComment freeBoardComment = FreeBoardComment.builder()
                .freeBoardPost(freeBoardPost)
                .member(member)
                .content(freeBoardCommentWriteDTO.getContent())
                .isParent(true)
                .build();

        freeBoardCommentRepository.save(freeBoardComment);

        freeBoardComment.setGroupId(freeBoardComment.getId());
        freeBoardCommentRepository.save(freeBoardComment);
    }

    public void reply(FreeBoardCommentReplyDTO boardCommentReplyDTO, String memberId) {
        Member member = memberRepository.findById(Long.parseLong(memberId))
                .orElseThrow(InvalidValue::new);

        FreeBoardComment freeBoardComment = freeBoardCommentRepository.findByIdWithNotDelete(Long.parseLong(boardCommentReplyDTO.getCommentId()));
        if (freeBoardComment == null) throw new InvalidValue();

        FreeBoardComment freeBoardComment_save = FreeBoardComment.builder()
                .member(member)
                .freeBoardPost(freeBoardComment.getFreeBoardPost())
                .groupId(freeBoardComment.getGroupId())
                .content(boardCommentReplyDTO.getContent())
                .build();

        freeBoardCommentRepository.save(freeBoardComment_save);
    }

    public List<FreeBoardCommentsDTO> getComments(Long postId, int page) {
        Pageable pageable = PageRequest.of(page, CommentPageSize.BASIC,
                Sort.by("groupId").ascending()
                        .and(Sort.by("isParent").descending()
                                .and(Sort.by("createdDate").ascending()
                                )
                        )
        );

        Iterable<FreeBoardComment> iterableComments = freeBoardCommentRepository.findByFreeBoardPostId(postId, pageable);

        List<FreeBoardComment> freeBoardComments = StreamSupport.stream(iterableComments.spliterator(), false)
                .collect(Collectors.toList());

        ModelMapper modelMapper = new ModelMapper();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss");

        return freeBoardComments.stream()
                .map(c -> {
                    FreeBoardCommentsDTO dto = modelMapper.map(c, FreeBoardCommentsDTO.class);
                    String strCreatedDate = c.getCreatedDate().format(formatter);
                    dto.setCreatedDate(strCreatedDate);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public int getTotalCount(Long postId) {
        return (int) freeBoardCommentRepository.getTotalCount(postId);
    }
}
