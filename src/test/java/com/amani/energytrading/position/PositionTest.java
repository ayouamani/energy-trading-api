package com.amani.energytrading.position;

import com.amani.energytrading.trade.Commodity;
import com.amani.energytrading.trade.Direction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PositionTest {

    private static final Instant NOW = Instant.parse("2026-11-15T10:00:00Z");

    private final Position position = Position.open("PF-1", Commodity.POWER, LocalDate.of(2026, 12, 1), NOW);

    @Test
    void newPositionIsFlat() {
        assertThat(position.getNetQuantityMwh()).isEqualByComparingTo("0");
        assertThat(position.getNotionalEur()).isEqualByComparingTo("0");
    }

    @Test
    void buyIncreasesTheNetQuantityAndTheNotional() {
        position.apply(Direction.BUY, new BigDecimal("10"), new BigDecimal("85.50"), NOW);

        assertThat(position.getNetQuantityMwh()).isEqualByComparingTo("10");
        assertThat(position.getNotionalEur()).isEqualByComparingTo("855");
    }

    @Test
    void sellDecreasesTheNetQuantityAndTheNotional() {
        position.apply(Direction.BUY, new BigDecimal("10"), new BigDecimal("80"), NOW);
        position.apply(Direction.SELL, new BigDecimal("4"), new BigDecimal("90"), NOW);

        assertThat(position.getNetQuantityMwh()).isEqualByComparingTo("6");
        assertThat(position.getNotionalEur()).isEqualByComparingTo("440");
    }

    @Test
    void positionCanGoShort() {
        position.apply(Direction.SELL, new BigDecimal("5"), new BigDecimal("70"), NOW);

        assertThat(position.getNetQuantityMwh()).isEqualByComparingTo("-5");
        assertThat(position.getNotionalEur()).isEqualByComparingTo("-350");
    }

    @Test
    void negativePricesAreSupported() {
        position.apply(Direction.BUY, new BigDecimal("20"), new BigDecimal("-12.5"), NOW);

        assertThat(position.getNetQuantityMwh()).isEqualByComparingTo("20");
        assertThat(position.getNotionalEur()).isEqualByComparingTo("-250");
    }
}
