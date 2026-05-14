package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.dto.response.PaymentResponse;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Payment;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import dev.tmmc.ulms.objects.entities.enums.PaymentStatus;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.exceptions.PaymentDeclinedException;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.PaymentMapper;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.PaymentRepository;
import dev.tmmc.ulms.objects.services.payment.PaymentGatewayClient;
import dev.tmmc.ulms.objects.services.payment.PaymentGatewayClient.ChargeResult;
import dev.tmmc.ulms.security.OwnershipChecker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FineRepository fineRepository;
    private final PaymentGatewayClient gateway;
    private final AuditService auditService;

    public PaymentService(PaymentRepository paymentRepository,
                          FineRepository fineRepository,
                          PaymentGatewayClient gateway,
                          AuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.fineRepository = fineRepository;
        this.gateway = gateway;
        this.auditService = auditService;
    }

    @Transactional(noRollbackFor = PaymentDeclinedException.class)
    public Payment processPayment(Integer fineId, PaymentMethod method, User user) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found: " + fineId));

        if (fine.getStatus() == FineStatus.PAID) {
            throw new LoanStateException("Fine is already paid.");
        }

        ChargeResult result = gateway.charge(fine.getAmount(), method, fineId);

        Payment payment = new Payment();
        payment.setFine(fine);
        payment.setUser(user);
        payment.setAmount(fine.getAmount());
        payment.setPayment_date(OffsetDateTime.now());
        payment.setMethod(method);

        if (result.success()) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setProviderRef(result.providerRef());
            fine.setStatus(FineStatus.PAID);
            fineRepository.save(fine);
            Payment saved = paymentRepository.save(payment);
            auditService.record(AuditAction.PAY_FINE_SUCCESS,
                    "fineId=" + fineId + " amount=" + fine.getAmount() + " method=" + method);
            return saved;
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setDeclineReason(result.declineReason());
        paymentRepository.save(payment);
        auditService.record(AuditAction.PAY_FINE_FAILED,
                "fineId=" + fineId + " reason=" + result.declineReason());
        throw new PaymentDeclinedException(result.declineReason());
    }

    @Transactional(noRollbackFor = PaymentDeclinedException.class)
    public PaymentResponse processPaymentAsResponse(Integer fineId, PaymentMethod method, User user) {
        return PaymentMapper.toResponse(processPayment(fineId, method, user));
    }

    @Transactional(readOnly = true)
    public List<Payment> findByUser(User user) {
        return paymentRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByUserAsResponse(User user) {
        return paymentRepository.findByUser(user).stream()
                .map(PaymentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<Payment> findByUser(User user, Pageable pageable) {
        return paymentRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findById(Integer id) {
        return paymentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponse> findByIdAsResponse(Integer id) {
        return paymentRepository.findById(id)
                .map(payment -> {
                    OwnershipChecker.requireOwnerOrStaff(payment.getUser().getId());
                    return PaymentMapper.toResponse(payment);
                });
    }

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findAllAsResponse() {
        return paymentRepository.findAll().stream()
                .map(PaymentMapper::toResponse)
                .toList();
    }
}
