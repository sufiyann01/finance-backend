package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.FinancialRecordDto;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.TransactionType;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import com.zorvyn.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    public Page<FinancialRecordDto.Response> getAll(
            TransactionType type, String category,
            LocalDate startDate, LocalDate endDate,
            Pageable pageable) {

        return recordRepository
                .findWithFilters(type, category, startDate, endDate, pageable)
                .map(FinancialRecordDto.Response::from);
    }

    public FinancialRecordDto.Response getById(Long id) {
        return FinancialRecordDto.Response.from(findOrThrow(id));
    }

    public FinancialRecordDto.Response create(FinancialRecordDto.CreateRequest request) {
        User currentUser = getCurrentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .deleted(false)
                .build();

        return FinancialRecordDto.Response.from(recordRepository.save(record));
    }

    public FinancialRecordDto.Response update(Long id, FinancialRecordDto.UpdateRequest request) {
        FinancialRecord record = findOrThrow(id);

        if (request.getAmount() != null) record.setAmount(request.getAmount());
        if (request.getType() != null) record.setType(request.getType());
        if (request.getCategory() != null) record.setCategory(request.getCategory().trim());
        if (request.getDate() != null) record.setDate(request.getDate());
        if (request.getNotes() != null) record.setNotes(request.getNotes());

        return FinancialRecordDto.Response.from(recordRepository.save(record));
    }

    // Soft delete - marks deleted instead of removing from DB
    public void delete(Long id) {
        FinancialRecord record = findOrThrow(id);
        record.setDeleted(true);
        record.setDeletedAt(LocalDateTime.now());
        recordRepository.save(record);
    }

    private FinancialRecord findOrThrow(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
