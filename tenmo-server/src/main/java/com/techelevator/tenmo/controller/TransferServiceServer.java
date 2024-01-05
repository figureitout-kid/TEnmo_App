package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;

import java.math.BigDecimal;
@Transactional
@Service
public class TransferServiceServer {

    //this class is where the business logic of handling the sending and receiving of transfers should reside.


    private AccountDao accountDao;
    private TransferDao transferDao;
    private UserDao userDao;

    @Autowired
    public TransferServiceServer(TransferDao transferDao, AccountDao accountDao, UserDao userDao) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    public TransferServiceServer() {

    }


    @Transactional
    public Transfer sendBucks(int senderAccountId, int receiverAccountId, BigDecimal transferAmount) {
        // Implement all the checks needed: sender is not sending to themselves, sender has enough money,
        // sender is sending more than zero, etc

        // Get the current balance
        BigDecimal senderBalance = accountDao.getBalance(senderAccountId);

        if (senderBalance == null) {
            // Handle the case where the sender balance is null
            throw new IllegalStateException("Balance cannot be null.");
        }

        //create Transfer object
        Transfer transfer = new Transfer();
        transfer.setTransferTypeId(2); // shouldn't hard code fix later
        transfer.setTransferStatusId(2); //shouldn't hard code fix later
        transfer.setAccountFrom(senderAccountId);
        transfer.setAccountTo(receiverAccountId);
        transfer.setAmount(transferAmount);


        //ensure funds. not sending to self. and positive transfer amount.
        validateTransfer(transfer, senderBalance);

        // Create a transfer record
        Transfer createdTransfer = transferDao.createTransfer(transfer);

        // If the transfer was successful, update both account balances
        if (createdTransfer != null) {
            updateSenderBalance(senderAccountId, transferAmount); // Subtract from sender
            updateReceiverBalance(receiverAccountId, transferAmount); // Add to receiver
        }

        return createdTransfer;
    }

    private void validateTransfer(Transfer transfer, BigDecimal senderBalance) {
        if (senderBalance.compareTo(transfer.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds for the transfer.");
        }
        if (transfer.getAccountFrom() == transfer.getAccountTo()) {
            throw new IllegalStateException("Cannot transfer to the same account.");
        }
        if (transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Transfer amount must be greater than zero.");
        }
    }


    private void updateSenderBalance(int accountId, BigDecimal amount) {
        BigDecimal senderBalance = accountDao.getBalance(accountId);
        accountDao.updateBalance(accountId, senderBalance.subtract(amount));
    }

    private void updateReceiverBalance(int accountId, BigDecimal amount) {
        BigDecimal receiverBalance = accountDao.getBalance(accountId);
        accountDao.updateBalance(accountId, receiverBalance.add(amount));
    }
}
// Check if the sender has enough balance to make the transfer
//        if (senderBalance.compareTo(amount) < 0) {
//        throw new IllegalStateException("Insufficient funds for the transfer.");
//        }
//
//        // Ensure the sender is not sending money to themselves
//        if (accountFrom == accountTo) {
//        throw new IllegalStateException("Cannot transfer to the same account.");
//        }
//
//        // Check for a zero or negative amount
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//        throw new IllegalStateException("Transfer amount must be greater than zero.");
//        }



//        BigDecimal balance = accountService.getCurrentBalance();
//
//        int recipientAccountId = consoleService.promptForInt("Enter the recipient's Account ID: ");
//        BigDecimal transferAmount = consoleService.promptForBigDecimal("Enter the amount to transfer: ");
//        if (recipientAccountId == currentUser.getUser().getId()) {
//            System.out.println("You cannot send money to yourself.");
//            return;
//        }
//
//
//
//        if (transferAmount.compareTo(balance) > 0) {
//            System.out.println("You do not have enough funds to complete this transfer.");
//            return;
//        }
//
//        Transfer transfer = new Transfer();
//        transfer.setAccountFrom(currentUser.getUser().getId());
//        transfer.setAccountTo(recipientAccountId);
//        transfer.setAmount(transferAmount);
//        transfer.setTransferTypeId(2); //send?
//        transfer.setTransferStatusId(2); //approved?
//
//
//        Transfer result = transferService.createTransfer(transfer);
//        if (result != null) {
//            System.out.println("Transfer completed successfully!");
//            BigDecimal updatedBalance = accountService.getCurrentBalance();
//            consoleService.printBalance(updatedBalance);
//        } else {
//            System.out.println("Transfer failed.");
//        }
//    } catch (Exception e) {
//        consoleService.printErrorMessage();
//    }

