package com.xincao9.prs.stream.processor;

import com.hankcs.hanlp.HanLP;
import com.xincao9.prs.api.constant.ConfigConsts;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author xincao9@gmail.com
 */
@Component
public class RawTextProcessor extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawTextProcessor.class);

    @PostConstruct
    public void initMethod() {
        start();
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, RawTextProcessor.class.getSimpleName());
        props.put(StreamsConfig.CLIENT_ID_CONFIG, RawTextProcessor.class.getSimpleName());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> kStream = builder.stream(ConfigConsts.RAW_TEXT_TOPIC);
        kStream.filter((String key, String value) -> StringUtils.isNotBlank(value)).foreach((String key, String value) -> {
            List<String> keywords = HanLP.extractKeyword(value, 5);
            LOGGER.info("content = {}, words = {}", value, keywords);
        });
        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), props);
        kafkaStreams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }

}