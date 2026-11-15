package com.amani.energytrading.position;

import com.amani.energytrading.event.TradeBookedEvent;
import com.amani.energytrading.trade.Commodity;
import com.amani.energytrading.trade.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    private static final LocalDate DELIVERY = LocalDate.of(2026, 12, 1);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private PositionRepository positionRepository;
    @Mock
    private ProcessedEventRepository processedEventRepository;

    private PositionService service;

    @BeforeEach
    void setUp() {
        service = new PositionService(positionRepository, processedEventRepository, CLOCK);
    }

    @Test
    void opensAPositionOnTheFirstTrade() {
        TradeBookedEvent event = event(Direction.BUY, "25", "60");
        when(processedEventRepository.existsById(event.eventId())).thenReturn(false);
        when(positionRepository.findByPortfolioAndCommodityAndDeliveryDate("PF-1", Commodity.GAS, DELIVERY))
                .thenReturn(Optional.empty());

        boolean applied = service.apply(event);

        assertThat(applied).isTrue();
        ArgumentCaptor<Position> saved = ArgumentCaptor.forClass(Position.class);
        verify(positionRepository).save(saved.capture());
        assertThat(saved.getValue().getNetQuantityMwh()).isEqualByComparingTo("25");
        assertThat(saved.getValue().getNotionalEur()).isEqualByComparingTo("1500");
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void updatesTheExistingPosition() {
        Position existing = Position.open("PF-1", Commodity.GAS, DELIVERY, CLOCK.instant());
        existing.apply(Direction.BUY, new BigDecimal("25"), new BigDecimal("60"), CLOCK.instant());
        TradeBookedEvent event = event(Direction.SELL, "10", "65");
        when(processedEventRepository.existsById(event.eventId())).thenReturn(false);
        when(positionRepository.findByPortfolioAndCommodityAndDeliveryDate("PF-1", Commodity.GAS, DELIVERY))
                .thenReturn(Optional.of(existing));

        service.apply(event);

        assertThat(existing.getNetQuantityMwh()).isEqualByComparingTo("15");
        assertThat(existing.getNotionalEur()).isEqualByComparingTo("850");
    }

    @Test
    void ignoresAnEventThatWasAlreadyProcessed() {
        TradeBookedEvent event = event(Direction.BUY, "25", "60");
        when(processedEventRepository.existsById(event.eventId())).thenReturn(true);

        boolean applied = service.apply(event);

        assertThat(applied).isFalse();
        verify(positionRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    private static TradeBookedEvent event(Direction direction, String quantity, String price) {
        return new TradeBookedEvent(UUID.randomUUID(), UUID.randomUUID(), "PF-1", Commodity.GAS, direction,
                new BigDecimal(quantity), new BigDecimal(price), DELIVERY, CLOCK.instant());
    }
}
