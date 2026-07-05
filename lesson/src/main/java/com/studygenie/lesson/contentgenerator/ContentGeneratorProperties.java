package com.studygenie.lesson.contentgenerator;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "content-generator")
@Setter
@Getter
public class ContentGeneratorProperties {

    private String authUrl;
    private String completionsUrl;
    private String accessKey;
    private String secret;
    private String tenantName;
    private int timeoutSeconds = 20;
}
