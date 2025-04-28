package com.kartingrm.Repositories;

import com.kartingrm.Entities.ClientEntity;
import com.kartingrm.Entities.SpecialDaysEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SpecialDayRepository  extends JpaRepository<SpecialDaysEntity, Long> {

    Optional<SpecialDaysEntity> findByDate(LocalDate date);


}
