package com.xincao9.prs.stream.repository;

import com.xincao9.prs.api.model.RawTextArticle;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 *
 * @author xincao9@gmail.com
 */
public interface RawTextArticleRepository extends ElasticsearchRepository<RawTextArticle, Long> {

}