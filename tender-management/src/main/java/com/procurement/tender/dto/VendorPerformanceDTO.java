package com.procurement.tender.dto;

// Java 25 Records as GraphQL DTO
public record VendorPerformanceDTO(
        double averageTechnicalScore,
        double averageFinancialScore,
        double weightedFinalScore,
        double totalBidsSubmitted
) {}
