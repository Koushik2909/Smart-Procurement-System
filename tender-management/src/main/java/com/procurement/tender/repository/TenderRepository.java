package com.procurement.tender.repository;

import com.procurement.tender.domain.Tender;
import com.procurement.tender.domain.TenderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenderRepository extends JpaRepository<Tender, Long> {
    List<Tender> findByStatus(TenderStatus status);
}
