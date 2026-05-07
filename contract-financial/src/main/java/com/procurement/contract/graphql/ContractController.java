package com.procurement.contract.graphql;

import com.procurement.contract.domain.Contract;
import com.procurement.contract.domain.Payment;
import com.procurement.contract.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ContractController {

    @Autowired
    private ContractService contractService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Contract getContractDetails(@Argument Long id) {
        return contractService.getContractById(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Contract> getAwardHistory(@Argument Long vendorId) {
        return contractService.getAwardHistory(vendorId);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Payment> getPaymentDetails(@Argument Long contractId) {
        return contractService.getPaymentsByContract(contractId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Contract awardContract(@Argument Long tenderId, @Argument Long vendorId, @Argument Double contractValue) {
        return contractService.awardContract(tenderId, vendorId, contractValue);
    }

    @MutationMapping
    @PreAuthorize("hasRole('VENDOR') or hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Contract signContract(@Argument Long id, @Argument String digitalSignature) {
        return contractService.signContract(id, digitalSignature);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Contract terminateContract(@Argument Long id, @Argument String reason) {
        return contractService.terminateContract(id, reason);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Payment createPaymentSchedule(@Argument Long contractId, @Argument String milestoneName,
                                          @Argument Double amount, @Argument String dueDate) {
        return contractService.createPaymentSchedule(contractId, milestoneName, amount, dueDate);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Payment releasePayment(@Argument Long paymentId) {
        return contractService.releasePayment(paymentId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Payment processPenalty(@Argument Long paymentId, @Argument Double penaltyAmount) {
        return contractService.processPenalty(paymentId, penaltyAmount);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Payment refundDeposit(@Argument Long paymentId) {
        return contractService.refundDeposit(paymentId);
    }
}
