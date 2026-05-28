package com.jeez.zp.platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ZpServicePlatformConfigurationGuardTest {

    @Test
    @Timeout(60)
    void applicationComponentScanDoesNotExplicitlyScanCnDev33() {
        ComponentScan componentScan = ZpServicePlatformApplication.class.getAnnotation(ComponentScan.class);
        assertNotNull(componentScan, "application should keep explicit component scan for platform/common packages");

        assertFalse(Arrays.asList(componentScan.basePackages()).contains("cn.dev33"));
        assertTrue(Arrays.asList(componentScan.basePackages()).contains("com.jeez.zp.platform"));
        assertTrue(Arrays.asList(componentScan.basePackages()).contains("com.jeez.common"));
    }

    @Test
    @Timeout(60)
    void applicationYamlUsesDatasourceEnvironmentPlaceholders() throws IOException {
        String content = readClasspathResource("application.yml");

        assertTrue(content.contains("username: ${SPRING_DATASOURCE_USERNAME"));
        assertTrue(content.contains("password: ${SPRING_DATASOURCE_PASSWORD"));
        assertFalse(content.contains("username: root"));
        assertFalse(content.contains("password: 123456"));
    }

    @Test
    @Timeout(60)
    void applicationDockerYamlUsesDatasourceEnvironmentPlaceholders() throws IOException {
        String content = readClasspathResource("application-docker.yml");

        assertTrue(content.contains("username: ${SPRING_DATASOURCE_USERNAME"));
        assertTrue(content.contains("password: ${SPRING_DATASOURCE_PASSWORD"));
        assertFalse(content.contains("username: ${SPRING_DATASOURCE_USERNAME:root}"));
        assertFalse(content.contains("password: ${SPRING_DATASOURCE_PASSWORD:123456}"));
    }

    private String readClasspathResource(String resourceName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(inputStream, () -> "resource not found: " + resourceName);
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
