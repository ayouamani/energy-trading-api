package com.amani.energytrading.trade;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload to book a trade. The price may be negative: negative power prices are a reality
 * on European day-ahead markets when renewable production exceeds demand.
 */
public record BookTradeRequest(
        @NotBlank @Size(max = 64) String portfolio,
        @NotNull Commodity commodity,
        @NotNull Direction direction,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal quantityMwh,
        @NotNull @Digits(integer = 15, fraction = 4) BigDecimal priceEurPerMwh,
        @NotNull @FutureOrPresent LocalDate deliveryDate) {
}
