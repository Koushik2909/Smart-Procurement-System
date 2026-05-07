package com.procurement.tender.dto;

// Java 25 Records as GraphQL DTO
public record ProcurementInsightsDTO(
        long totalTenders,
        long activeTenders,
        long awardedTenders,
        long cancelledTenders,
        String reportGeneratedAt
) {}
