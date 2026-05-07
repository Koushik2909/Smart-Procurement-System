package com.procurement.bid.domain.state;

public sealed interface BidState permits 
    BidState.Created, 
    BidState.Submitted, 
    BidState.Locked, 
    BidState.Opened, 
    BidState.Evaluated, 
    BidState.Accepted, 
    BidState.Rejected {

    record Created() implements BidState {}
    record Submitted() implements BidState {}
    record Locked() implements BidState {}
    record Opened() implements BidState {}
    record Evaluated(double technicalScore, double financialScore) implements BidState {}
    record Accepted() implements BidState {}
    record Rejected(String reason) implements BidState {}
}
