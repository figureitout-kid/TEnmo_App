package com.techelevator.tenmo.controller;


import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

//no implementation of updateBalance here using an endpoint-- google says that's not really a secure way to alter balances???
@RestController
@RequestMapping("/account") //let's check this for correct path??
public class AccountController {
    private final AccountDao accountDao;
    private final UserDao userDao;


    //potential token insertion below, based off authenticaitonController??????????????
    public AccountController(AccountDao accountDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    @GetMapping("/balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getBalance(Principal principal) {
        String username = principal.getName();
        User user = userDao.getUserByUsername(username);

        if (user != null) {
            int userId = user.getId();
            Integer accountId = accountDao.getAccountIdByUserId(userId);

            if(accountId != null) {
                BigDecimal balance = accountDao.getBalance(accountId);
                return new ResponseEntity<>(balance, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    private int findUserIdByUsername(String username) {
        User user = userDao.getUserByUsername(username);
        if (user != null) {
            return user.getId();
        } else {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User: " + username + " not found.");
    }
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Account> getAccountByAccountId(@PathVariable int accountId, Principal principal) {
        int userId = findUserIdByUsername(principal.getName());
        Account account = accountDao.getAccountByAccountId(accountId);
        if (account != null && account.getUserId() == userId){
            return new ResponseEntity<>(account, HttpStatus.OK);
        } else if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access to the account is forbidden.");
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Integer> getAccountIdByUserId(@PathVariable int userId) {
        Integer accountId = accountDao.getAccountIdByUserId(userId);
        if (accountId != null) {
            return new ResponseEntity<>(accountId, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Account> listAllAccounts() {
        return accountDao.listAllAccounts();
    }


}
