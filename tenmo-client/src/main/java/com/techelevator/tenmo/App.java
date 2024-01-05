package com.techelevator.tenmo;

import com.techelevator.tenmo.controller.TransferServiceServer;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;

import java.math.BigDecimal;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final TransferService transferService = new TransferService();
    private final TransferServiceServer transferServiceServer = new TransferServiceServer();
    private AuthenticatedUser currentUser;

    public App() {
    }

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }


    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
		// TODO Auto-generated method stub
		try {
            int userId = currentUser.getUser().getId();
            accountService.setAuthToken(currentUser.getToken());

            Integer accountId = accountService.getAccountIdByUserId(userId);
            if (accountId != null) {
                BigDecimal balance = accountService.getCurrentBalance();
                consoleService.printBalance(balance);
            } else {
                System.out.println("Account ID not found for the user.");
            }
        } catch (Exception e) {
            consoleService.printErrorMessage();
        }
	}


//TODO view transfer history needs to be adjusted to accomodate for current methods taking getting AccountId, change
    //here not in TransferService or any other method...KAREN can do this
    private void viewTransferHistory() {
        // TODO Auto-generated method stub
        try {
            transferService.setAuthToken(currentUser.getToken());
            List<Transfer> transferHistory = transferService.getTransferByAccountId(currentUser.getUser().getId());

            if (transferHistory != null && !transferHistory.isEmpty()) {
                for (Transfer transfer : transferHistory) {
                    System.out.println("Transfer ID: " + transfer.getTransferId());
                    System.out.println("From Account ID: " + transfer.getAccountFrom());
                    System.out.println("To Account ID: " + transfer.getAccountTo());
                    System.out.println("Amount: $: " + transfer.getAmount());
                    System.out.println("-------------------------------------");
                }
            } else {
                System.out.println("No transfer history available.");
            }
        } catch (Exception e) {
            consoleService.printErrorMessage();
        }
    }

    private void viewPendingRequests() {
        // TODO Auto-generated method stub
        try {
            transferService.setAuthToken(currentUser.getToken());
            List<Transfer> pendingRequests = transferService.getPendingTransfers(currentUser.getUser().getId());
            if (pendingRequests != null && !pendingRequests.isEmpty()) {
                for (Transfer request : pendingRequests) {
                    System.out.println("Request ID: " + request.getTransferId());
                    System.out.println("From Account ID: " + request.getAccountFrom());
                    System.out.println("Amount: $" + request.getAmount());
                    System.out.println("------------------------------------------");

                }
            } else {
                System.out.println("No pending requests.");
            }
        } catch (Exception e) {
            consoleService.printErrorMessage();
        }
    }

    private void sendBucks() {
        try {
            int senderAccountId = accountService.getAccountIdByUserId(currentUser.getUser().getId());

            // Receive input from the client
            int receiverAccountId = consoleService.promptForInt("Enter the recipient's Account ID: ");
            BigDecimal transferAmount = consoleService.promptForBigDecimal("Enter the amount to transfer: ");


            // Send transfer to the server to handle the logic
            Transfer result = transferServiceServer.sendBucks(senderAccountId, receiverAccountId, transferAmount);
            if (result != null) {
                System.out.println("Transfer completed successfully!");
            } else {
                System.out.println("Transfer failed.");
            }
        } catch (Exception e) {
            consoleService.printErrorMessage();
        }
    }

    private void updateBalanceOnTransfer(BigDecimal transferAmount) {
        // Update the balance on the client side
        try {
            BigDecimal currentBalance = accountService.getCurrentBalance();

            consoleService.printBalance(currentBalance);


            // Add logging statements
            System.out.println("Current Balance before update: " + currentBalance);
            System.out.println("Transfer Amount: " + transferAmount);

            BigDecimal updatedBalance = currentBalance.subtract(transferAmount);
            System.out.println("Updated Balance: " + updatedBalance);

            accountService.setCurrentBalance(updatedBalance);

        } catch (Exception e) {
            // Print the exception stack trace for debugging
            e.printStackTrace();
            consoleService.printErrorMessage();
        }
    }


    private void requestBucks() {
        // TODO Auto-generated method stub
        int requestedAccountId = consoleService.promptForInt("Enter the Account ID to request from: ");
        BigDecimal requestAmount = consoleService.promptForBigDecimal("Enter the request amount: ");

        Transfer requestTransfer = new Transfer();
        requestTransfer.setAccountFrom(currentUser.getUser().getId());
        requestTransfer.setAccountTo(requestedAccountId);
        requestTransfer.setAmount(requestAmount);
        requestTransfer.setTransferTypeId(1); //request?
        requestTransfer.setTransferStatusId(1); //pending?

        try {
            transferService.setAuthToken(currentUser.getToken());
            Transfer result = transferService.createTransfer(requestTransfer);
            if (result != null) {
                System.out.println("Request sent successfully!");
            } else {
                System.out.println("Request failed.");
            }
        } catch (Exception e) {
            consoleService.printErrorMessage();
        }

    }

    private void afterLogin() {
        accountService.setAuthToken(currentUser.getToken());
        transferService.setAuthToken(currentUser.getToken());
    }
}

