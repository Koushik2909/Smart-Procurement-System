package com.procurement.contract.service;

import com.procurement.contract.domain.Contract;
import com.procurement.contract.domain.ContractStatus;
import com.procurement.contract.domain.Payment;
import com.procurement.contract.domain.PaymentStatus;
import com.procurement.contract.repository.ContractRepository;
import com.procurement.contract.repository.PaymentRepository;
import com.procurement.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractService Unit Tests")
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ContractService contractService;

    private Contract generatedContract;
    private Contract signedContract;
    private Payment scheduledPayment;

    @BeforeEach
    void setUp() {
        generatedContract = new Contract();
        generatedContract.setId(1L);
        generatedContract.setTenderId(10L);
        generatedContract.setVendorId(2L);
        generatedContract.setContractValue(500000.0);
        generatedContract.setStatus(ContractStatus.GENERATED);

        signedContract = new Contract();
        signedContract.setId(2L);
        signedContract.setTenderId(10L);
        signedContract.setVendorId(2L);
        signedContract.setContractValue(500000.0);
        signedContract.setStatus(ContractStatus.SIGNED);
        signedContract.setDigitalSignature("SIG_ABC123");

        scheduledPayment = new Payment();
        scheduledPayment.setId(1L);
        scheduledPayment.setContractId(1L);
        scheduledPayment.setMilestoneName("Phase 1 Delivery");
        scheduledPayment.setAmount(100000.0);
        scheduledPayment.setStatus(PaymentStatus.SCHEDULED);
    }

    // --- Contract Tests ---

    @Test
    @DisplayName("awardContract — should create contract with GENERATED status")
    void awardContract_ShouldReturnGeneratedContract() {
        when(contractRepository.save(any(Contract.class))).thenReturn(generatedContract);

        Contract result = contractService.awardContract(10L, 2L, 500000.0);

        assertThat(result.getStatus()).isEqualTo(ContractStatus.GENERATED);
        assertThat(result.getTenderId()).isEqualTo(10L);
        assertThat(result.getVendorId()).isEqualTo(2L);
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    @DisplayName("signContract — GENERATED contract should transition to SIGNED")
    void signContract_ShouldSetSignedStatus() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(generatedContract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract result = contractService.signContract(1L, "DIGITAL_SIG_XYZ");

        assertThat(result.getStatus()).isEqualTo(ContractStatus.SIGNED);
        assertThat(result.getDigitalSignature()).isEqualTo("DIGITAL_SIG_XYZ");
        assertThat(result.getSignedAt()).isNotNull();
    }

    @Test
    @DisplayName("signContract — non-GENERATED contract should throw IllegalStateException")
    void signContract_WhenAlreadySigned_ShouldThrowException() {
        when(contractRepository.findById(2L)).thenReturn(Optional.of(signedContract));

        assertThatThrownBy(() -> contractService.signContract(2L, "SIG_AGAIN"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GENERATED");
    }

    @Test
    @DisplayName("activateContract — SIGNED contract should transition to ACTIVE")
    void activateContract_ShouldSetActiveStatus() {
        when(contractRepository.findById(2L)).thenReturn(Optional.of(signedContract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract result = contractService.activateContract(2L);

        assertThat(result.getStatus()).isEqualTo(ContractStatus.ACTIVE);
    }

    @Test
    @DisplayName("activateContract — non-SIGNED contract should throw IllegalStateException")
    void activateContract_WhenNotSigned_ShouldThrowException() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(generatedContract));

        assertThatThrownBy(() -> contractService.activateContract(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SIGNED");
    }

    @Test
    @DisplayName("terminateContract — should set TERMINATED status with reason")
    void terminateContract_ShouldSetTerminatedStatus() {
        when(contractRepository.findById(2L)).thenReturn(Optional.of(signedContract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract result = contractService.terminateContract(2L, "Vendor non-compliance");

        assertThat(result.getStatus()).isEqualTo(ContractStatus.TERMINATED);
        assertThat(result.getTerminatedReason()).isEqualTo("Vendor non-compliance");
    }

    @Test
    @DisplayName("getContractById — non-existing ID should throw ResourceNotFoundException")
    void getContractById_NotFound_ShouldThrowException() {
        when(contractRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.getContractById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Contract not found");
    }

    @Test
    @DisplayName("getAwardHistory — should return contracts for vendor")
    void getAwardHistory_ShouldReturnVendorContracts() {
        when(contractRepository.findByVendorId(2L)).thenReturn(List.of(generatedContract, signedContract));

        List<Contract> result = contractService.getAwardHistory(2L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> c.getVendorId().equals(2L));
    }

    // --- Payment Tests ---

    @Test
    @DisplayName("createPaymentSchedule — should save payment with SCHEDULED status")
    void createPaymentSchedule_ShouldReturnScheduledPayment() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(scheduledPayment);

        Payment result = contractService.createPaymentSchedule(
                1L, "Phase 1 Delivery", 100000.0,
                LocalDateTime.now().plusMonths(1).toString()
        );

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SCHEDULED);
        assertThat(result.getMilestoneName()).isEqualTo("Phase 1 Delivery");
    }

    @Test
    @DisplayName("releasePayment — should change status to RELEASED")
    void releasePayment_ShouldSetReleasedStatus() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(scheduledPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = contractService.releasePayment(1L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.RELEASED);
        assertThat(result.getReleasedAt()).isNotNull();
    }

    @Test
    @DisplayName("processPenalty — should set penalty amount and PENALTY_APPLIED status")
    void processPenalty_ShouldApplyPenalty() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(scheduledPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = contractService.processPenalty(1L, 5000.0);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENALTY_APPLIED);
        assertThat(result.getPenaltyAmount()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("refundDeposit — should change payment status to REFUNDED")
    void refundDeposit_ShouldSetRefundedStatus() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(scheduledPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = contractService.refundDeposit(1L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("releasePayment — non-existing payment should throw ResourceNotFoundException")
    void releasePayment_NotFound_ShouldThrowException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.releasePayment(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment not found");
    }
}
