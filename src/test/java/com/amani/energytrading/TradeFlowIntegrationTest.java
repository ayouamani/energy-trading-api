package com.amani.energytrading;

import com.amani.energytrading.event.TradeBookedEvent;
import com.amani.energytrading.position.PositionResponse;
import com.amani.energytrading.trade.BookTradeRequest;
import com.amani.energytrading.trade.Commodity;
import com.amani.energytrading.trade.Direction;
import com.amani.energytrading.trade.TradeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end test on real infrastructure (PostgreSQL + Kafka started by Testcontainers):
 * REST call -> database -> Kafka -> consumer -> position projection.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TradeFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private KafkaTemplate<String, TradeBookedEvent> kafkaTemplate;

    @Test
    void bookedTradesAreProjectedIntoNetPositions() {
        String portfolio = "PF-" + UUID.randomUUID();
        LocalDate delivery = LocalDate.now().plusDays(7);

        ResponseEntity<TradeResponse> buy = rest.postForEntity("/api/trades",
                new BookTradeRequest(portfolio, Commodity.POWER, Direction.BUY,
                        new BigDecimal("100"), new BigDecimal("80"), delivery), TradeResponse.class);
        rest.postForEntity("/api/trades",
                new BookTradeRequest(portfolio, Commodity.POWER, Direction.SELL,
                        new BigDecimal("30"), new BigDecimal("90"), delivery), TradeResponse.class);

        assertThat(buy.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(rest.getForEntity("/api/trades/{id}", TradeResponse.class, buy.getBody().id()).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            PositionResponse[] positions = rest.getForObject("/api/positions?portfolio={p}",
                    PositionResponse[].class, portfolio);
            assertThat(positions).hasSize(1);
            assertThat(positions[0].netQuantityMwh()).isEqualByComparingTo("70");
            assertThat(positions[0].notionalEur()).isEqualByComparingTo("5300");
        });
    }

    @Test
    void aRedeliveredEventIsAppliedOnlyOnce() {
        String portfolio = "PF-" + UUID.randomUUID();
        LocalDate delivery = LocalDate.now().plusDays(7);
        TradeBookedEvent first = event(portfolio, delivery, "10");
        TradeBookedEvent second = event(portfolio, delivery, "5");

        // Same key => same partition => processed in order: first, first (duplicate), second
        kafkaTemplate.send("trades.booked", portfolio, first);
        kafkaTemplate.send("trades.booked", portfolio, first);
        kafkaTemplate.send("trades.booked", portfolio, second);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            PositionResponse[] positions = rest.getForObject("/api/positions?portfolio={p}",
                    PositionResponse[].class, portfolio);
            assertThat(positions).hasSize(1);
            assertThat(positions[0].netQuantityMwh()).isEqualByComparingTo("15");
        });
    }

    private static TradeBookedEvent event(String portfolio, LocalDate delivery, String quantity) {
        return new TradeBookedEvent(UUID.randomUUID(), UUID.randomUUID(), portfolio, Commodity.GAS, Direction.BUY,
                new BigDecimal(quantity), new BigDecimal("40"), delivery, Instant.now());
    }
}
