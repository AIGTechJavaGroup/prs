package com.xincao9.prs.stream.processor;

import com.xincao9.prs.api.constant.ConfigConsts;
import com.xincao9.prs.stream.entity.RawTextArticleDO;
import com.xincao9.prs.stream.repository.RawTextArticleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 计算用户的短期标签
 *
 * @author xincao9@gmail.com
 */
@Component
public class UserRawTextArticleProcessor extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRawTextArticleProcessor.class);

    @Autowired
    private RawTextArticleRepository articleRepository;

    @PostConstruct
    public void initMethod() {
        start();
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, UserRawTextArticleProcessor.class.getSimpleName());
        props.put(StreamsConfig.CLIENT_ID_CONFIG, UserRawTextArticleProcessor.class.getSimpleName());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> kStream = builder.stream(ConfigConsts.USER_RAW_TEXT_ARTICLE_TOPIC);
        KTable<String, Long> kTable = kStream.filter((String key, String value) -> StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)).flatMapValues((value) -> {
            List<RawTextArticleDO> articleDOs = articleRepository.findByTitle(value);
            if (articleDOs == null || articleDOs.isEmpty()) {
                return null;
            }
            Set<String> keywords = new HashSet();
            for (RawTextArticleDO articleDO : articleDOs) {
                keywords.addAll(articleDO.getTextKeywords());
            }
            return keywords;
        }).groupBy((key, keyword) -> String.format("%s:%s", key, keyword)).count();
        kTable.toStream().foreach(
                (key, value) -> {
                    LOGGER.info("key = {}, value = {}", key, value);
                }
        );
        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), props);
        kafkaStreams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }

}
