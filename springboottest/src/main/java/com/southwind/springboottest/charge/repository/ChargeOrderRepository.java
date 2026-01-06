package com.southwind.springboottest.charge.repository;

import com.southwind.springboottest.charge.entity.ChargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChargeOrderRepository extends JpaRepository<ChargeOrder, Long> {
    List<ChargeOrder> findByUserIdOrderByStartTimeDesc(Long userId);
    List<ChargeOrder> findByUserId(Long userId);
    Optional<ChargeOrder> findFirstByPileIdAndStatusOrderByStartTimeDesc(Long pileId, String status);
}
