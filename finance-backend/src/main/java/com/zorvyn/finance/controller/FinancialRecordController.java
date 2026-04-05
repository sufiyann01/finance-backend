package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.ApiResponse;
import com.zorvyn.finance.dto.FinancialRecordDto;
import com.zorvyn.finance.model.TransactionType;
import com.zorvyn.finance.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@AllArgsConstructor
public class FinancialRecordController {

    private FinancialRecordService recordService;


    @GetMapping
    public ResponseEntity<ApiResponse<Page<FinancialRecordDto.Response>>> getAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(
                recordService.getAll(type, category, startDate, endDate, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialRecordDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(recordService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FinancialRecordDto.Response>> create(
            @Valid @RequestBody FinancialRecordDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Record created", recordService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialRecordDto.Response>> update(
            @PathVariable Long id,
            @RequestBody FinancialRecordDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Record updated", recordService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        recordService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Record deleted", null));
    }
}
