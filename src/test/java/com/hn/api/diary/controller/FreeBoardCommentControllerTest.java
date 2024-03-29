package com.hn.api.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hn.api.diary.dto.freeBoard.FreeBoardCommentReplyDTO;
import com.hn.api.diary.dto.freeBoard.FreeBoardCommentUpdateDTO;
import com.hn.api.diary.dto.freeBoard.FreeBoardCommentWriteDTO;
import com.hn.api.diary.entity.FreeBoardComment;
import com.hn.api.diary.entity.FreeBoardPost;
import com.hn.api.diary.entity.Member;
import com.hn.api.diary.repository.FreeBoardCommentRepository;
import com.hn.api.diary.repository.FreeBoardPostRepository;
import com.hn.api.diary.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

@AutoConfigureMockMvc
@SpringBootTest
class FreeBoardCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FreeBoardPostRepository freeBoardPostRepository;
    @Autowired
    private FreeBoardCommentRepository freeBoardCommentRepository;

    @BeforeEach
    void clean() {
        freeBoardCommentRepository.deleteAll();
        freeBoardPostRepository.deleteAll();
        memberRepository.deleteAll();
        new FreeBoardTestData().given(memberRepository, freeBoardPostRepository, freeBoardCommentRepository);
    }

    @Test
    void delete() throws Exception {
        // given
        HashMap<String, Object> map = new FreeBoardTestData().signIn(memberRepository, objectMapper, mockMvc);
        Cookie[] cookies = (Cookie[]) map.get("cookies");

        List<FreeBoardComment> comments = freeBoardCommentRepository.findAllWithNotDelete();
        FreeBoardComment comment1 = comments.get(0);
        FreeBoardComment comment2 = comments.get(1);
        FreeBoardComment comment4 = comments.get(2);
        FreeBoardComment comment5 = comments.get(3);

        ResultActions actions_1 = mockMvc.perform(MockMvcRequestBuilders.delete("/freeBoard/comment/delete/"+comment1.getId())
                .cookie(cookies));

        ResultActions actions_2 = mockMvc.perform(MockMvcRequestBuilders.delete("/freeBoard/comment/delete/"+comment2.getId())
                .cookie(cookies));

        ResultActions actions_4 = mockMvc.perform(MockMvcRequestBuilders.delete("/freeBoard/comment/delete/"+comment4.getId())
                .cookie(cookies));

        ResultActions actions_5 = mockMvc.perform(MockMvcRequestBuilders.delete("/freeBoard/comment/delete/"+comment5.getId())
                .cookie(cookies));

        // then
        actions_1.andExpect(MockMvcResultMatchers.status().isOk());
        actions_2.andExpect(MockMvcResultMatchers.status().isForbidden());
        actions_4.andExpect(MockMvcResultMatchers.status().isForbidden());
        actions_5.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void update() throws Exception {
        // given
        HashMap<String, Object> map = new FreeBoardTestData().signIn(memberRepository, objectMapper, mockMvc);
        Member member = (Member) map.get("member");
        Cookie[] cookies = (Cookie[]) map.get("cookies");

        List<FreeBoardComment> comments = freeBoardCommentRepository.findAllWithNotDelete();
        Long commentId_1 = comments.stream()
                .filter(c -> c.getMember().getId() == member.getId())
                .findFirst()
                .map(FreeBoardComment::getId)
                .orElseThrow(NoSuchElementException::new);
        Long commentId_2 = comments.stream()
                .filter(c -> c.getMember().getId() != member.getId())
                .findFirst()
                .map(FreeBoardComment::getId)
                .orElseThrow(NoSuchElementException::new);

        FreeBoardCommentUpdateDTO dto1 = FreeBoardCommentUpdateDTO.builder()
                .commentId(commentId_1.toString())
                .content("update")
                .build();
        FreeBoardCommentUpdateDTO dto2 = FreeBoardCommentUpdateDTO.builder()
                .commentId(commentId_2.toString())
                .content("update")
                .build();

        String json1 = objectMapper.writeValueAsString(dto1);
        String json2 = objectMapper.writeValueAsString(dto2);

        // when
        ResultActions actions1 = mockMvc.perform(MockMvcRequestBuilders.put("/freeBoard/comment/update")
                .cookie(cookies)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json1)
        );
        ResultActions actions2 = mockMvc.perform(MockMvcRequestBuilders.put("/freeBoard/comment/update")
                .cookie(cookies)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2)
        );

        // then
        actions1.andExpect(MockMvcResultMatchers.status().isOk());
        actions2.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void write() throws Exception {
        // given
        HashMap<String, Object> map = new FreeBoardTestData().signIn(memberRepository, objectMapper, mockMvc);
        Cookie[] cookies = (Cookie[]) map.get("cookies");

        List<FreeBoardPost> posts = freeBoardPostRepository.findAllWithNotDelete();
        FreeBoardPost post = posts.get( new Random().nextInt(posts.size()) );

        FreeBoardCommentWriteDTO dto = FreeBoardCommentWriteDTO.builder()
                .postId(post.getId().toString())
                .content("content")
                .build();
        String json = objectMapper.writeValueAsString(dto);

        // when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.post("/freeBoard/comment/write")
                .cookie(cookies)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        );

        // then
        actions.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void reply() throws Exception {
        // given
        HashMap<String, Object> map = new FreeBoardTestData().signIn(memberRepository, objectMapper, mockMvc);
        Member member = (Member) map.get("user");
        Cookie[] cookies = (Cookie[]) map.get("cookies");

        List<FreeBoardComment> comments = freeBoardCommentRepository.findAllWithNotDelete();
        FreeBoardComment comment = comments.get( new Random().nextInt(comments.size()) );

        FreeBoardCommentReplyDTO dto = FreeBoardCommentReplyDTO.builder()
                .commentId(comment.getId().toString())
                .content("reply")
                .build();
        String json = objectMapper.writeValueAsString(dto);

        // when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.post("/freeBoard/comment/reply")
                .cookie(cookies)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        );

        // then
        actions.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getComments() throws Exception {
        // given
        List<FreeBoardPost> posts = freeBoardPostRepository.findAllWithNotDelete();
        FreeBoardPost post = posts.get( new Random().nextInt(posts.size()) );

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/freeBoard/comments/"+post.getId())
                .param("page", "1")
        );

        // expect
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getTotalCount() throws Exception {
        // given
        List<FreeBoardPost> posts = freeBoardPostRepository.findAllWithNotDelete();
        FreeBoardPost post = posts.get( new Random().nextInt(posts.size()) );

        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/freeBoard/comments/totalCount/"+post.getId()));

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
    }
}