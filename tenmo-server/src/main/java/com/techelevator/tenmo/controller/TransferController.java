package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/transfers")
public class TransferController {
    private final TransferDao transferDao;
    private final AccountDao accountDao;
    private final UserDao userDao;
    private final TransferServiceServer transferServiceServer;



    @Autowired
    public TransferController(TransferDao transferDao, AccountDao accountDao, UserDao userDao, TransferServiceServer transferServiceServer) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.transferServiceServer = transferServiceServer;
    }

    @PostMapping
    public ResponseEntity<Transfer> createTransfer(@RequestBody Transfer transfer) {
        Transfer createdTransfer = transferDao.createTransfer(transfer);

        if (createdTransfer != null) {
            return new ResponseEntity<>(createdTransfer, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{accountId}")
    public List<Transfer> getTransfersByAccountId(@PathVariable int accountId) {
        return transferDao.getTransfersByAccountId(accountId);
    }

    @GetMapping("/{transferId}")
    public ResponseEntity<Transfer> findTransferById(@PathVariable int transferId) {
        Transfer transfer = transferDao.findTransferById(transferId);
        if (transfer != null) {
            return new ResponseEntity<>(transfer, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/pending/{accountId}")
    public ResponseEntity<List<Transfer>> getPendingTransfers(@PathVariable int accountId) {
        List<Transfer> transfers = transferDao.getPendingTransfers(accountId);
        if (!transfers.isEmpty()) {
            return new ResponseEntity<>(transfers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    //////////////////////////////////////////methods needs authorization
    @GetMapping("/user/{username}/transfers")
    public List<Transfer> getTransfersByUsername(@PathVariable String username){
        List<Transfer> transfers = transferDao.getTransfersByUsername(username);
        if (transfers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No transfers found for user: " + username);
        }
        return transfers;
    }

    @PutMapping("/{transferId}/status/{statusId}")
    public ResponseEntity<?> updateTransferStatus(@PathVariable int transferId, @PathVariable int statusId) {
        boolean success = transferDao.updateTransferStatus(transferId, statusId);
        if (!success) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found with id: " + transferId);
        }
        return ResponseEntity.ok("Transfer status updated successfully.");
    }

//    @PostMapping("/send")
//    public ResponseEntity<?> sendBucks(@RequestBody Transfer transfer, Principal principal) {
//        try {
//
//            Transfer createdTransfer = transferServiceServer.sendBucks(transfer);
////            Transfer createdTransfer = transferServiceServer.sendBucks(
////                    transfer.getTransferTypeId(),
////                    transfer.getTransferStatusId(),
////                    transfer.getAccountFrom(),
////                    transfer.getAccountTo(),
////                    transfer.getAmount()
////
////            );
//            if (createdTransfer != null) {
//                return new ResponseEntity<>(createdTransfer, HttpStatus.CREATED);
//            } else {
//                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        } catch (IllegalStateException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }





//will need to change permissions once things are working properly.....................................................
//    @PreAuthorize("permitAll")
//    @PostMapping("/send")
//    public ResponseEntity<Transfer> sendBucks(@Valid @RequestBody Transfer transfer, Principal principal) {
//        User currentUser = userDao.getUserByUsername(principal.getName());
//        Integer senderAccountId = accountDao.getAccountIdByUserId(currentUser.getId());
//        if (senderAccountId == null || senderAccountId != transfer.getAccountFrom()) {
//            return new ResponseEntity<>("You can only transfer money from your own account.", HttpStatus.FORBIDDEN);
//        }
//
//        try {
//            Transfer createdTransfer = transferServiceServer.sendBucks(transfer);
//            if (createdTransfer != null) {
//                return new ResponseEntity<>(createdTransfer, HttpStatus.CREATED);
//            } else {
//                return new ResponseEntity<>("Could not complete transfer.", HttpStatus.BAD_REQUEST);
//            }
//        } catch (IllegalStateException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }
}

