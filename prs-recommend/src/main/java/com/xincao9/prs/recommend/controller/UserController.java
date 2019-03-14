package com.xincao9.prs.recommend.controller;

import com.xincao9.prs.recommend.entity.RawTextArticleDO;
import com.xincao9.prs.recommend.repository.RawTextArticleRepository;
import io.micrometer.core.instrument.util.StringUtils;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author xincao9@gmail.com
 */
@RestController
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RawTextArticleRepository rawTextArticleRepository;

    @GetMapping("{userId}/raw_text_article")
    public ResponseEntity<List<RawTextArticleDO>> rawTextArticle(@PathVariable String userId) {
        if (StringUtils.isBlank(userId)) {
            return ResponseEntity.status(400).build();
        }
        try {
            Set<String> keywords = redisTemplate.opsForZSet().reverseRange(userId, 0, 4);
            List<RawTextArticleDO> rawTextArticles = rawTextArticleRepository.findByTextKeywordsIn(keywords);
            return ResponseEntity.ok(rawTextArticles);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(500).build();
        }

    }
}
