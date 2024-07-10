package com.example.demo.controller;

import com.example.demo.model.article.Article;
import com.example.demo.repository.UserClickDetailRepository;
import com.example.demo.service.ArticleService;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ArticleController.class)
@WithMockUser(username = "janedoe", password = "test1235")
public class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserClickDetailRepository userClickDetailRepository;


    @Test
    public void testGetAllArticles() throws Exception {
        // Mock data (an empty ArrayList)
        List<Article> mockArticles = new ArrayList<>();

        // Mock articleService
        given(articleService.getAllArticles(anyInt(), anyInt())).willReturn(mockArticles);
        given(articleService.countTotalArticles()).willReturn(mockArticles.size());

        // Execute Get Request
        mockMvc.perform(get("/api/1.0/articles")
                        .param("paging", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articles", hasSize(mockArticles.size())));
    }
}