package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.dto.response.PaymentResponse;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Payment;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import dev.tmmc.ulms.objects.entities.enums.PaymentStatus;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.PaymentMapper;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.PaymentRepository;
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

    public PaymentService(PaymentRepository paymentRepository,
                          FineRepository fineRepository) {
        this.paymentRepository = paymentRepository;
        this.fineRepository = fineRepository;
    }

    @Transactional
    public Payment processPayment(Integer fineId, PaymentMethod method, User user) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found: " + fineId));

        if (fine.getStatus() == FineStatus.PAID) {
            throw new LoanStateException("Fine is already paid.");
        }

        Payment payment = new Payment();
        payment.setFine(fine);
        payment.setUser(user);
        payment.setAmount(fine.getAmount());
        payment.setPayment_date(OffsetDateTime.now());
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.COMPLETED);

        fine.setStatus(FineStatus.PAID);
        fineRepository.save(fine);

        return paymentRepository.save(payment);
    }

    @Transactional
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
        return paymentRepository.findById(id).map(PaymentMapper::toResponse);
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
