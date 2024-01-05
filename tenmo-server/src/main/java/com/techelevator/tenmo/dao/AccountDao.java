package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    BigDecimal getBalance(int accountId);
    void updateBalance(int accountId, BigDecimal amount);
    Account getAccountByUserId(int userId);
    List<Account> listAllAccounts();
    Account getAccountByAccountId(int accountId);
    Integer getAccountIdByUserId(int userId);

}
