package com.procurement.contract.service;

import com.procurement.contract.domain.Contract;
import com.procurement.contract.domain.ContractStatus;
import com.procurement.contract.domain.Payment;
import com.procurement.contract.domain.PaymentStatus;
import com.procurement.contract.repository.ContractRepository;
import com.procurement.contract.repository.PaymentRepository;
import com.procurement.core.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public Contract awardContract(Long tenderId, Long vendorId, Double contractValue) {
        Contract contract = new Contract();
        contract.setTenderId(tenderId);
        contract.setVendorId(vendorId);
        contract.setContractValue(contractValue);
        contract.setStatus(ContractStatus.GENERATED);
        return contractRepository.save(contract);
    }

    public Contract signContract(Long id, String digitalSignature) {
        Contract contract = getContractById(id);
        if (contract.getStatus() != ContractStatus.GENERATED) {
            throw new IllegalStateException("Contract must be in GENERATED status to sign");
        }
        contract.setDigitalSignature(digitalSignature);
        contract.setSignedAt(LocalDateTime.now());
        contract.setStatus(ContractStatus.SIGNED);
        return contractRepository.save(contract);
    }

    public Contract activateContract(Long id) {
        Contract contract = getContractById(id);
        if (contract.getStatus() != ContractStatus.SIGNED) {
            throw new IllegalStateException("Contract must be SIGNED to activate");
        }
        contract.setStatus(ContractStatus.ACTIVE);
        return contractRepository.save(contract);
    }

    public Contract terminateContract(Long id, String reason) {
        Contract contract = getContractById(id);
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setTerminatedReason(reason);
        return contractRepository.save(contract);
    }

    public Contract getContractById(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
    }

    public List<Contract> getAwardHistory(Long vendorId) {
        return contractRepository.findByVendorId(vendorId);
    }

    // --- Payment Operations ---

    public Payment createPaymentSchedule(Long contractId, String milestoneName, Double amount, String dueDate) {
        Payment payment = new Payment();
        payment.setContractId(contractId);
        payment.setMilestoneName(milestoneName);
        payment.setAmount(amount);
        payment.setDueDate(LocalDateTime.parse(dueDate));
        payment.setStatus(PaymentStatus.SCHEDULED);
        return paymentRepository.save(payment);
    }

    public Payment releasePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.RELEASED);
        payment.setReleasedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment processPenalty(Long paymentId, Double penaltyAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setPenaltyAmount(penaltyAmount);
        payment.setStatus(PaymentStatus.PENALTY_APPLIED);
        return paymentRepository.save(payment);
    }

    public Payment refundDeposit(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByContract(Long contractId) {
        return paymentRepository.findByContractId(contractId);
    }
}
