package com.hn.api.diary.controller;

import com.hn.api.diary.dto.BoardPostsDTO;
import com.hn.api.diary.dto.PostBoardDTO;
import com.hn.api.diary.entity.Board;
import com.hn.api.diary.response.DataResponse;
import com.hn.api.diary.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class BoardController {

    private final BoardService boardService;

    @PostMapping(value = "/board/post")
    public void post(@RequestBody PostBoardDTO postBoardDTO, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        boardService.post(postBoardDTO, userId);

        System.out.println("post");
    }

    @GetMapping(value = "/board/posts/all")
    public ResponseEntity<DataResponse<BoardPostsDTO>> getAllPosts(){
        List<BoardPostsDTO> posts = boardService.getAllPosts();

        DataResponse<BoardPostsDTO> response = DataResponse.<BoardPostsDTO>builder()
                .data(posts)
                .build();

        ResponseEntity<DataResponse<BoardPostsDTO>> responseEntity
                = ResponseEntity.status(response.getStatus()).body(response);

        return responseEntity;
    }
}
