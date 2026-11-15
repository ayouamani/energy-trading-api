package com.amani.energytrading.trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TradeResponse(
        UUID id,
        String portfolio,
        Commodity commodity,
        Direction direction,
        BigDecimal quantityMwh,
        BigDecimal priceEurPerMwh,
        LocalDate deliveryDate,
        TradeStatus status,
        Instant bookedAt) {

    public static TradeResponse from(Trade trade) {
        return new TradeResponse(trade.getId(), trade.getPortfolio(), trade.getCommodity(), trade.getDirection(),
                trade.getQuantityMwh(), trade.getPriceEurPerMwh(), trade.getDeliveryDate(), trade.getStatus(),
                trade.getBookedAt());
    }
}
