package com.amani.energytrading.trade;

import com.amani.energytrading.event.TradeBookedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T10:00:00Z");

    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TradeService service;

    @BeforeEach
    void setUp() {
        service = new TradeService(tradeRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void booksTheTradeAndPublishesAnEvent() {
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BookTradeRequest request = new BookTradeRequest("PF-1", Commodity.POWER, Direction.BUY,
                new BigDecimal("50"), new BigDecimal("92.40"), LocalDate.of(2026, 12, 1));

        Trade trade = service.book(request);

        assertThat(trade.getId()).isNotNull();
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.BOOKED);
        assertThat(trade.getBookedAt()).isEqualTo(NOW);

        ArgumentCaptor<TradeBookedEvent> event = ArgumentCaptor.forClass(TradeBookedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().tradeId()).isEqualTo(trade.getId());
        assertThat(event.getValue().eventId()).isNotNull();
        assertThat(event.getValue().portfolio()).isEqualTo("PF-1");
        assertThat(event.getValue().direction()).isEqualTo(Direction.BUY);
        assertThat(event.getValue().quantityMwh()).isEqualByComparingTo("50");
    }

    @Test
    void throwsWhenTheTradeDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(tradeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(TradeNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}
