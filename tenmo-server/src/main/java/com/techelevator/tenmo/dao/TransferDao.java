package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {
    Transfer createTransfer(Transfer transfer);
    List<Transfer> getTransfersByAccountId(int accountId);
    Transfer findTransferById(int transferId);
    List<Transfer> getPendingTransfers(int accountId);
    List<Transfer> getTransfersByUsername(String username);
    boolean updateTransferStatus(int transferId, int transferStatusId);
}

//    int transferTypeId, int transferStatusId, int accountFrom, int accountTo, BigDecimal amount
