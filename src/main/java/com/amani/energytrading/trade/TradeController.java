package com.amani.energytrading.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trades", description = "Trade capture")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    @Operation(summary = "Book a new trade")
    public ResponseEntity<TradeResponse> book(@Valid @RequestBody BookTradeRequest request,
                                              UriComponentsBuilder uriBuilder) {
        Trade trade = tradeService.book(request);
        URI location = uriBuilder.path("/api/trades/{id}").buildAndExpand(trade.getId()).toUri();
        return ResponseEntity.created(location).body(TradeResponse.from(trade));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a trade by id")
    public TradeResponse get(@PathVariable("id") UUID id) {
        return TradeResponse.from(tradeService.get(id));
    }

    @GetMapping
    @Operation(summary = "List the trades of a portfolio, most recent first")
    public List<TradeResponse> list(@RequestParam("portfolio") String portfolio) {
        return tradeService.findByPortfolio(portfolio).stream().map(TradeResponse::from).toList();
    }
}
