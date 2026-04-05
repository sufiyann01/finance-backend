package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.DashboardSummaryDto;
import com.zorvyn.finance.dto.FinancialRecordDto;
import com.zorvyn.finance.model.TransactionType;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    public DashboardSummaryDto getSummary() {

        BigDecimal totalIncome = recordRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);


        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        for (Object[] row : recordRepository.sumByCategory()) {
            String categoryKey;


            if (row[0] instanceof TransactionType) {
                categoryKey = ((TransactionType) row[0]).name();
            } else {
                categoryKey = row[0].toString();
            }

            categoryTotals.put(categoryKey, (BigDecimal) row[1]);
        }


        List<FinancialRecordDto.Response> recentActivity = recordRepository
                .findTop10ByDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(FinancialRecordDto.Response::from)
                .collect(Collectors.toList());


        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        List<DashboardSummaryDto.MonthlyTrendDto> monthlyTrends = buildMonthlyTrends(sixMonthsAgo);

        return DashboardSummaryDto.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryTotals(categoryTotals)
                .recentActivity(recentActivity)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    private List<DashboardSummaryDto.MonthlyTrendDto> buildMonthlyTrends(LocalDate startDate) {
        List<Object[]> rows = recordRepository.monthlyTrends(startDate);

        Map<String, DashboardSummaryDto.MonthlyTrendDto> trendMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String month = row[0].toString();


            TransactionType type;
            if (row[1] instanceof TransactionType) {
                type = (TransactionType) row[1];
            } else {
                type = TransactionType.valueOf(row[1].toString());
            }

            BigDecimal amount = (BigDecimal) row[2];

            trendMap.computeIfAbsent(month, m ->
                    DashboardSummaryDto.MonthlyTrendDto.builder()
                            .month(m)
                            .income(BigDecimal.ZERO)
                            .expenses(BigDecimal.ZERO)
                            .build()
            );

            DashboardSummaryDto.MonthlyTrendDto trend = trendMap.get(month);
            if (type == TransactionType.INCOME) {
                trend.setIncome(amount);
            } else if (type == TransactionType.EXPENSE) {
                trend.setExpenses(amount);
            }
        }

        return new ArrayList<>(trendMap.values());
    }
}
