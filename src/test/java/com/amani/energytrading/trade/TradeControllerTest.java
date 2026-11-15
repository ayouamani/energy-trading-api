package com.amani.energytrading.trade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @Test
    void returns201WithLocationWhenTheTradeIsBooked() throws Exception {
        LocalDate delivery = LocalDate.now().plusDays(1);
        Trade trade = Trade.book("PF-1", Commodity.POWER, Direction.BUY, new BigDecimal("50"),
                new BigDecimal("92.40"), delivery, Instant.parse("2026-11-15T10:00:00Z"));
        when(tradeService.book(any(BookTradeRequest.class))).thenReturn(trade);

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"portfolio":"PF-1","commodity":"POWER","direction":"BUY",
                                 "quantityMwh":50,"priceEurPerMwh":92.40,"deliveryDate":"%s"}
                                """.formatted(delivery)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/trades/" + trade.getId())))
                .andExpect(jsonPath("$.id").value(trade.getId().toString()))
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    void returns400ProblemDetailWhenThePayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"portfolio":"","commodity":"POWER","direction":"BUY",
                                 "quantityMwh":-5,"priceEurPerMwh":92.40,"deliveryDate":"2020-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(400));

        verify(tradeService, never()).book(any());
    }

    @Test
    void returns404ProblemDetailWhenTheTradeDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(tradeService.get(id)).thenThrow(new TradeNotFoundException(id));

        mockMvc.perform(get("/api/trades/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Trade not found"))
                .andExpect(jsonPath("$.detail").value(containsString(id.toString())));
    }
}
