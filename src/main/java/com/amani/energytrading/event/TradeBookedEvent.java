package com.amani.energytrading.event;

import com.amani.energytrading.trade.Commodity;
import com.amani.energytrading.trade.Direction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Integration event published on Kafka once a trade has been committed to the database.
 * The {@code eventId} lets consumers process each event exactly once.
 */
public record TradeBookedEvent(
        UUID eventId,
        UUID tradeId,
        String portfolio,
        Commodity commodity,
        Direction direction,
        BigDecimal quantityMwh,
        BigDecimal priceEurPerMwh,
        LocalDate deliveryDate,
        Instant bookedAt) {
}
