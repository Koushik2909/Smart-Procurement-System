package com.procurement.tender.dto;

// Java 25 Records as GraphQL DTO
public record TenderAnalyticsDTO(
        long DRAFT,
        long PUBLISHED,
        long OPEN,
        long CLOSED,
        long EVALUATION,
        long AWARDED,
        long CANCELLED
) {}
