package com.amani.energytrading.trade;

import com.amani.energytrading.event.TradeBookedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TradeService {

    private static final Logger log = LoggerFactory.getLogger(TradeService.class);

    private final TradeRepository tradeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public TradeService(TradeRepository tradeRepository, ApplicationEventPublisher eventPublisher, Clock clock) {
        this.tradeRepository = tradeRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /**
     * Persists the trade and raises a {@link TradeBookedEvent}. The event is forwarded to Kafka
     * only after the transaction commits (see {@link TradeEventPublisher}).
     */
    @Transactional
    public Trade book(BookTradeRequest request) {
        Trade trade = tradeRepository.save(Trade.book(
                request.portfolio(),
                request.commodity(),
                request.direction(),
                request.quantityMwh(),
                request.priceEurPerMwh(),
                request.deliveryDate(),
                Instant.now(clock)));

        eventPublisher.publishEvent(trade.toBookedEvent());
        log.info("Booked trade {} {} {} MWh {} for portfolio {}", trade.getId(), trade.getDirection(),
                trade.getQuantityMwh(), trade.getCommodity(), trade.getPortfolio());
        return trade;
    }

    @Transactional(readOnly = true)
    public Trade get(UUID id) {
        return tradeRepository.findById(id).orElseThrow(() -> new TradeNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Trade> findByPortfolio(String portfolio) {
        return tradeRepository.findByPortfolioOrderByBookedAtDesc(portfolio);
    }
}
