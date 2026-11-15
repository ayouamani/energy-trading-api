package com.amani.energytrading.trade;

import java.util.UUID;

public class TradeNotFoundException extends RuntimeException {

    public TradeNotFoundException(UUID id) {
        super("Trade " + id + " not found");
    }
}
