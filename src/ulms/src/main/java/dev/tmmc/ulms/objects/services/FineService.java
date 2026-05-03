package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.dto.response.FineResponse;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.FineMapper;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FineService {

    private static final BigDecimal FINE_PER_DAY = new BigDecimal("1.00");

    private final FineRepository fineRepository;

    public FineService(FineRepository fineRepository) {
        this.fineRepository = fineRepository;
    }

    @Transactional
    public Fine calculateFine(Loan loan) {
        LocalDate dueDate = loan.getDue_date().toLocalDate();
        LocalDate today = LocalDate.now();

        if (!today.isAfter(dueDate)) {
            throw new LoanStateException("Loan is not overdue; no fine applicable.");
        }

        long daysOverdue = today.toEpochDay() - dueDate.toEpochDay();
        BigDecimal amount = FINE_PER_DAY.multiply(BigDecimal.valueOf(daysOverdue));

        Fine fine = new Fine();
        fine.setFineId(UUID.randomUUID().toString());
        fine.setLoan(loan);
        fine.setAmount(amount);
        fine.setCalculated_date(Date.valueOf(today));
        fine.setStatus(FineStatus.UNPAID);

        return fineRepository.save(fine);
    }

    @Transactional(readOnly = true)
    public List<Fine> findByLoan(Loan loan) {
        return fineRepository.findByLoan(loan);
    }

    @Transactional(readOnly = true)
    public List<Fine> findByLoanUser(User user) {
        return fineRepository.findByLoanUser(user);
    }

    @Transactional(readOnly = true)
    public List<FineResponse> findByLoanUserAsResponse(User user) {
        return fineRepository.findByLoanUser(user).stream()
                .map(FineMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Fine> findUnpaidByUser(User user) {
        return fineRepository.findByLoanUserAndStatusNot(user, FineStatus.PAID);
    }

    @Transactional(readOnly = true)
    public List<FineResponse> findUnpaidByUserAsResponse(User user) {
        return fineRepository.findByLoanUserAndStatusNot(user, FineStatus.PAID).stream()
                .map(FineMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> sumUnpaidByUser(User user) {
        return fineRepository.sumUnpaidByUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<Fine> findById(Integer id) {
        return fineRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<FineResponse> findByIdAsResponse(Integer id) {
        return fineRepository.findById(id).map(FineMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<Fine> findAll() {
        return fineRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FineResponse> findAllAsResponse() {
        return fineRepository.findAll().stream()
                .map(FineMapper::toResponse)
                .toList();
    }

    @Transactional
    public Fine markAsPaid(Integer fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found: " + fineId));
        fine.setStatus(FineStatus.PAID);
        return fineRepository.save(fine);
    }

    @Transactional
    public FineResponse markAsPaidAsResponse(Integer fineId) {
        return FineMapper.toResponse(markAsPaid(fineId));
    }
}
