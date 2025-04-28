package com.kartingrm.Repositories;

import com.kartingrm.Entities.IncomingReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface IncomingReportRepository extends JpaRepository<IncomingReportEntity, Long> {

    Optional<IncomingReportEntity> findById(Long id);

}
