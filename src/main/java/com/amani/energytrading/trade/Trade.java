package com.amani.energytrading.trade;

import com.amani.energytrading.event.TradeBookedEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String portfolio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Commodity commodity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Direction direction;

    @Column(name = "quantity_mwh", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityMwh;

    @Column(name = "price_eur_per_mwh", nullable = false, precision = 19, scale = 4)
    private BigDecimal priceEurPerMwh;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TradeStatus status;

    @Column(name = "booked_at", nullable = false)
    private Instant bookedAt;

    protected Trade() {
        // for JPA
    }

    private Trade(String portfolio, Commodity commodity, Direction direction, BigDecimal quantityMwh,
                  BigDecimal priceEurPerMwh, LocalDate deliveryDate, Instant bookedAt) {
        this.id = UUID.randomUUID();
        this.portfolio = portfolio;
        this.commodity = commodity;
        this.direction = direction;
        this.quantityMwh = quantityMwh;
        this.priceEurPerMwh = priceEurPerMwh;
        this.deliveryDate = deliveryDate;
        this.status = TradeStatus.BOOKED;
        this.bookedAt = bookedAt;
    }

    public static Trade book(String portfolio, Commodity commodity, Direction direction, BigDecimal quantityMwh,
                             BigDecimal priceEurPerMwh, LocalDate deliveryDate, Instant bookedAt) {
        return new Trade(portfolio, commodity, direction, quantityMwh, priceEurPerMwh, deliveryDate, bookedAt);
    }

    public TradeBookedEvent toBookedEvent() {
        return new TradeBookedEvent(UUID.randomUUID(), id, portfolio, commodity, direction,
                quantityMwh, priceEurPerMwh, deliveryDate, bookedAt);
    }

    public UUID getId() {
        return id;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public Direction getDirection() {
        return direction;
    }

    public BigDecimal getQuantityMwh() {
        return quantityMwh;
    }

    public BigDecimal getPriceEurPerMwh() {
        return priceEurPerMwh;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public Instant getBookedAt() {
        return bookedAt;
    }
}
