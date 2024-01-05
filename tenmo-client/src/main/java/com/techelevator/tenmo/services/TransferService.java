package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class TransferService {

    private static final String API_BASE_URL = "http://localhost:8080/transfers";
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;



    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }


    public Transfer createTransfer(Transfer transfer) {
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(
                    API_BASE_URL,
                    HttpMethod.POST,
                    makeTransferEntity(transfer),
                    Transfer.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                return response.getBody();
            } else {
                System.out.println("Failed to create transfer. Status code: " + response.getStatusCode());
            }
        } catch (RestClientResponseException e) {
            handleRestClientResponseException(e);
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Cannot access the resource: " + e.getMessage());
        }
        return null;
    }

public List<Transfer> getTransferByAccountId(int accountId) {
    ResponseEntity<Transfer[]> response = restTemplate.exchange(
            API_BASE_URL + "/" + accountId,
            HttpMethod.GET,
            makeAuthEntity(),
            Transfer[].class
    );
    return Arrays.asList(response.getBody());
}

    public Transfer findTransferById(int transferId){
        ResponseEntity<Transfer> response = restTemplate.exchange(
                API_BASE_URL + "/" + transferId,
                HttpMethod.GET,
                makeAuthEntity(),
                Transfer.class
        );
        return response.getBody();
    }
    public List<Transfer> getPendingTransfers(int accountId) {
        ResponseEntity<Transfer[]> response = restTemplate.exchange(
                API_BASE_URL + "/pending/" + accountId,
                HttpMethod.GET,
                makeAuthEntity(),
                Transfer[].class
        );
        return Arrays.asList(response.getBody());
    }

    public List<Transfer> getTransfersByUsername(String username) {
        List<Transfer> transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(
                    API_BASE_URL + "/user/" + username +"/transfers",
                    HttpMethod.GET,
                    makeAuthEntity(),
                    Transfer[].class
            );
            Transfer[] transferArray = response.getBody();
            if (transferArray != null) {
                transfers = Arrays.asList(transferArray);
            }
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public boolean updateTransferStatus(int transferId, int statusId) {
        String url = API_BASE_URL + "/" + transferId + "/status/" + statusId;
        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    makeAuthEntity(),
                    Void.class
            );
            return true;
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getMessage());
            if (e.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
                System.out.println("Transfer not found with id: " + transferId);
            } else {
                System.out.println("An error occurred while updating the transfer status: " + e.getStatusText());
            }
            return false;
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Cannot access the resource: " + e.getMessage());
            return false;
        }
    }

//    public Transfer sendBucks(Transfer transfer) {
//        HttpEntity<Transfer> entity = makeTransferEntity(transfer);
//
//        ResponseEntity<Transfer> response = restTemplate.postForEntity(
//                API_BASE_URL + "/send",
//                entity,
//                Transfer.class
//        );
//        return response.getBody();
//    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }
    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transfer, headers);
    }

    private void handleRestClientResponseException(RestClientResponseException e) {
        BasicLogger.log(e.getMessage());
        if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
            System.out.println("Not authorized to perform this operation. Please log in.");
        } else if (e.getRawStatusCode() == HttpStatus.FORBIDDEN.value()) {
            System.out.println("Operation Forbidden. You cannot send money to this account.");
        } else if (e.getRawStatusCode() == HttpStatus.BAD_REQUEST.value()) {
            System.out.println("An error occurred while processing the transfer: " + e.getStatusText());
        } else {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}
