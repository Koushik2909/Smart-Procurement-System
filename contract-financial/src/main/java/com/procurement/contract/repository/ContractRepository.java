package com.procurement.contract.repository;

import com.procurement.contract.domain.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByTenderId(Long tenderId);
    List<Contract> findByVendorId(Long vendorId);
}
