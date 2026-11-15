package com.amani.energytrading.position;

import com.amani.energytrading.trade.Commodity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByPortfolioAndCommodityAndDeliveryDate(String portfolio, Commodity commodity,
                                                                  LocalDate deliveryDate);

    List<Position> findByPortfolioOrderByDeliveryDateAscCommodityAsc(String portfolio);
}
