package com.amani.energytrading.position;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@Tag(name = "Positions", description = "Net positions computed from the trade stream")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    @Operation(summary = "Net positions of a portfolio per commodity and delivery date")
    public List<PositionResponse> list(@RequestParam("portfolio") String portfolio) {
        return positionService.findByPortfolio(portfolio).stream().map(PositionResponse::from).toList();
    }
}
