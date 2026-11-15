package com.amani.energytrading.position;

import com.amani.energytrading.trade.Commodity;
import com.amani.energytrading.trade.Direction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Net position of a portfolio for one commodity and one delivery date.
 * Buying increases the net quantity, selling decreases it; the notional is the signed sum of quantity x price.
 */
@Entity
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String portfolio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Commodity commodity;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "net_quantity_mwh", nullable = false, precision = 19, scale = 4)
    private BigDecimal netQuantityMwh;

    @Column(name = "notional_eur", nullable = false, precision = 19, scale = 4)
    private BigDecimal notionalEur;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Position() {
        // for JPA
    }

    private Position(String portfolio, Commodity commodity, LocalDate deliveryDate, Instant openedAt) {
        this.portfolio = portfolio;
        this.commodity = commodity;
        this.deliveryDate = deliveryDate;
        this.netQuantityMwh = BigDecimal.ZERO;
        this.notionalEur = BigDecimal.ZERO;
        this.updatedAt = openedAt;
    }

    public static Position open(String portfolio, Commodity commodity, LocalDate deliveryDate, Instant openedAt) {
        return new Position(portfolio, commodity, deliveryDate, openedAt);
    }

    public void apply(Direction direction, BigDecimal quantityMwh, BigDecimal priceEurPerMwh, Instant at) {
        BigDecimal signedQuantity = direction == Direction.BUY ? quantityMwh : quantityMwh.negate();
        this.netQuantityMwh = netQuantityMwh.add(signedQuantity);
        this.notionalEur = notionalEur.add(signedQuantity.multiply(priceEurPerMwh));
        this.updatedAt = at;
    }

    public Long getId() {
        return id;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public BigDecimal getNetQuantityMwh() {
        return netQuantityMwh;
    }

    public BigDecimal getNotionalEur() {
        return notionalEur;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
