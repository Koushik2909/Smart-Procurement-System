package com.procurement.bid.dto;

public record BidInput(Long tenderId, String technicalProposalUrl, String encryptedFinancialProposal) {
}
