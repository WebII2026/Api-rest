package com.example.demo.controller;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    @Test
    void health_devuelveStatusServiceYVersion() {
        Map<String, String> respuesta = new HealthController().health();
        assertThat(respuesta)
                .containsEntry("status", "ok")
                .containsEntry("service", "demo")
                .containsEntry("version", "v2");
    }
}
