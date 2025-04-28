package com.kartingrm.Repositories;

import com.kartingrm.Entities.PricingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingRepository extends JpaRepository<PricingEntity, Long> {

    PricingEntity findTopByOrderByIdDesc();


}
