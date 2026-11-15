package com.amani.energytrading.position;

import com.amani.energytrading.trade.Commodity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PositionResponse(
        String portfolio,
        Commodity commodity,
        LocalDate deliveryDate,
        BigDecimal netQuantityMwh,
        BigDecimal notionalEur,
        Instant updatedAt) {

    public static PositionResponse from(Position position) {
        return new PositionResponse(position.getPortfolio(), position.getCommodity(), position.getDeliveryDate(),
                position.getNetQuantityMwh(), position.getNotionalEur(), position.getUpdatedAt());
    }
}
