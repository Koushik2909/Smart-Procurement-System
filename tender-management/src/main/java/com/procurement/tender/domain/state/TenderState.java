package com.procurement.tender.domain.state;

public sealed interface TenderState permits 
    TenderState.Draft, 
    TenderState.Published, 
    TenderState.Open, 
    TenderState.Closed, 
    TenderState.Evaluation, 
    TenderState.Awarded, 
    TenderState.Cancelled {

    record Draft() implements TenderState {}
    record Published() implements TenderState {}
    record Open() implements TenderState {}
    record Closed() implements TenderState {}
    record Evaluation() implements TenderState {}
    record Awarded() implements TenderState {}
    record Cancelled(String reason) implements TenderState {}
}
