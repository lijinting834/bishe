package com.southwind.springboottest.charge.repository;

import com.southwind.springboottest.charge.entity.ChargePile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargePileRepository extends JpaRepository<ChargePile, Long> {
    Optional<ChargePile> findByPileNo(String pileNo);
}
