package com.zorvyn.finance.dto;

import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinancialRecordDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        @NotNull(message = "Type is required (INCOME or EXPENSE)")
        private TransactionType type;

        @NotBlank(message = "Category is required")
        private String category;

        @NotNull(message = "Date is required")
        private LocalDate date;

        private String notes;
    }

    @Data
    public static class UpdateRequest {
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private TransactionType type;
        private String category;
        private LocalDate date;
        private String notes;
    }

    @Data
    public static class Response {
        private Long id;
        private BigDecimal amount;
        private String type;
        private String category;
        private LocalDate date;
        private String notes;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(FinancialRecord r) {
            Response res = new Response();
            res.id = r.getId();
            res.amount = r.getAmount();
            res.type = r.getType().name();
            res.category = r.getCategory();
            res.date = r.getDate();
            res.notes = r.getNotes();
            res.createdBy = r.getCreatedBy().getUsername();
            res.createdAt = r.getCreatedAt();
            res.updatedAt = r.getUpdatedAt();
            return res;
        }
    }
}
