package com.example.demo.service;

import com.example.demo.model.article.Article;
import com.example.demo.model.article.Category;
import com.example.demo.model.user.UserClickDetail;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserClickDetailRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@SpringBootTest()

@ActiveProfiles("test")
public class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserClickDetailRepository userClickDetailRepository;

    @Mock
    private CategoryRepository categoryRepository;


    @InjectMocks
    private ArticleService articleService;

    private List<Article> mockArticles;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);  // 初始化Mock物件

        mockArticles = new ArrayList<>();

        // mock article
        Article article1 = mock(Article.class);
        Article article2 = mock(Article.class);

        // mock article object behavior
        when(article1.getId()).thenReturn(1);
        when(article1.getTitle()).thenReturn("Article 1");
        when(article1.getContent()).thenReturn("Content 1");

        when(article2.getId()).thenReturn(2);
        when(article2.getTitle()).thenReturn("Article 2");
        when(article2.getContent()).thenReturn("Content 2");

        mockArticles.add(article1);
        mockArticles.add(article2);
    }

    // Test Failed
    @Test
    public void testGetAllArticles() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> page = new PageImpl<>(mockArticles, pageable, mockArticles.size());

        when(articleRepository.findAllArticlesByPage(any(Pageable.class), any(RedisTemplate.class), any(ObjectMapper.class)))
                .thenReturn(mockArticles);

        // Act
        List<Article> result = articleService.getAllArticles(0, 10);

        // Assert
        assertEquals(mockArticles.size(), result.size());
        assertEquals(mockArticles.get(0).getTitle(), result.get(0).getTitle());
        assertEquals(mockArticles.get(1).getTitle(), result.get(1).getTitle());
    }

    @Test
    public void testCalculateCosineSimilarityAndRecommendTopics() {
        // Arrange
        List<UserClickDetail> userClickDetails = new ArrayList<>();
//        userClickDetails.add(new UserClickDetail(1L, 5, 3, 2, 4, 1, new Category(1L, "Company News")));
//        userClickDetails.add(new UserClickDetail(2L, 4, 1, 3, 2, 5, new Category(2L, "Broad market news")));
//        when(userClickDetailRepository.findAll()).thenReturn(userClickDetails);

        Category category1 = new Category();
        category1.setId(3);
        category1.setCategory("Company Discussion");

        Category category2 = new Category();
        category2.setId(4);
        category2.setCategory("Advice Request");

        when(categoryRepository.findByCategory(ArgumentMatchers.eq("Company Discussion"))).thenReturn(category1);
        when(categoryRepository.findByCategory(ArgumentMatchers.eq("Advice Request"))).thenReturn(category2);

        // Act
        articleService.calculateCosineSimilarity();

        // Assert
        verify(userClickDetailRepository, times(1)).findAll();
        verify(categoryRepository, times(1)).findByCategory("Company Discussion");
        verify(categoryRepository, times(1)).findByCategory("Advice Request");
        verify(userClickDetailRepository, times(2)).save(any(UserClickDetail.class));
    }
}
