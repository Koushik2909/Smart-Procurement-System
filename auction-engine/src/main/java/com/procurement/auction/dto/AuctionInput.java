package com.procurement.auction.dto;

public record AuctionInput(Long tenderId, String startTime, String endTime, Double startingPrice, Double minimumDecrement) {
}
