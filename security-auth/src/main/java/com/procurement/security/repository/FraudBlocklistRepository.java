package com.procurement.security.repository;

import com.procurement.security.domain.FraudBlocklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudBlocklistRepository extends JpaRepository<FraudBlocklist, Long> {
    Optional<FraudBlocklist> findByUserId(Long userId);
}
