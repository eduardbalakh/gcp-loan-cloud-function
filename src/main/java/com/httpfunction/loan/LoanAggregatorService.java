package com.httpfunction.loan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LoanAggregatorService {

    private final LoanDataProvider loanDataProvider;

    public LoanAggregatorService(LoanDataProvider loanDataProvider) {
        this.loanDataProvider = loanDataProvider;
    }

    public void saveLoan(LoanDO loanDO) {
        loanDataProvider.saveLoan(loanDO);
    }

    public List<LoanDO> getAllLoansForBorrower(String borrowerId) {
        return loanDataProvider.getLoansByBorrowerId(borrowerId);
    }

    public List<LoanDO> getAllLoansForOwner(String ownerId) {
        return loanDataProvider.getLoansByOwnerId(ownerId);
    }

    public List<LoanDO> getAllLoansForOwnerAndBorrower(String ownerId, String borrowerId) {
        return getAllLoansForOwner(ownerId).stream()
                .filter(loan -> loan.getBorowerId().equals(borrowerId))
                .collect(Collectors.toList());
    }

    public List<AggregatedLoanDO> getAggregatedLoanForBorrower(String borrowerId) {
        return getAggregatedLoanList(loanDataProvider.getLoansByBorrowerId(borrowerId));
    }

    public List<AggregatedLoanDO> getAggregatedLoadForOwner(String ownerId) {
        return getAggregatedLoanList(loanDataProvider.getLoansByOwnerId(ownerId));
    }

    private List<AggregatedLoanDO> getAggregatedLoanList(List<LoanDO> listOfLoans) {
        Map<String, AggregatedLoanDO> map = new HashMap<>();
        listOfLoans.forEach(loanDO -> {
            if (map.containsKey(loanDO.getLoanOwnerId())) {
                AggregatedLoanDO oldAggregatedLoanDO = map.get(loanDO.getLoanOwnerId());
                AggregatedLoanDO newAggregatedLoanDO = mergeTwoAggregatedLoans(oldAggregatedLoanDO, loanDO);
                map.put(loanDO.getLoanOwnerId(), newAggregatedLoanDO);
            } else {
                map.put(loanDO.getLoanOwnerId(), new AggregatedLoanDO(loanDO.getLoanOwnerId(), loanDO.getBorowerId(), loanDO.getValue(), loanDO.getDateTime(), loanDO.getDateTime()));
            }
        });
        return new ArrayList<>(map.values());
    }

    private AggregatedLoanDO mergeTwoAggregatedLoans(AggregatedLoanDO oldAggregate, LoanDO newLoan) {
        AggregatedLoanDO newAggregatedLoanDO = new AggregatedLoanDO();
        newAggregatedLoanDO.setOwnerId(oldAggregate.getOwnerId());
        newAggregatedLoanDO.setValue(oldAggregate.getValue().add(newLoan.getValue()));
        newAggregatedLoanDO.setBorrowerId(oldAggregate.getBorrowerId());
        if (oldAggregate.getStartDate().isAfter(newLoan.getDateTime())) {
            newAggregatedLoanDO.setStartDate(newLoan.getDateTime());
        }
        if (oldAggregate.getEndDate().isBefore(newLoan.getDateTime())) {
            newAggregatedLoanDO.setEndDate(newLoan.getDateTime());
        }
        return newAggregatedLoanDO;
    }

}
