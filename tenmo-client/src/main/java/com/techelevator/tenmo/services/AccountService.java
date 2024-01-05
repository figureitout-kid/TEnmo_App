package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class AccountService {
    //should we use the path of 'accounts' instead????***************************************************
    private static final String API_BASE_URL = "http://localhost:8080/account";
    private static final String BALANCE_ENDPOINT = "/balance";
    private static final String TRANSFER_ENDPOINT = "/transfer";
    private static final String TRANSFER_HISTORY_ENDPOINT = "/transfer/history";
    private static final String TRANSFER_PENDING_ENDPOINT = "/transfer/pending";
    private static final String TRANSFER_REQUEST_ENDPOINT = "/transfer/request";
    private static final String ACCOUNT_API_BASE_URL = "http://localhost:8080/account";
    private String apiUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    private BigDecimal currentBalance;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public AccountService(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public BigDecimal getCurrentBalance() {
        try {
            ResponseEntity<BigDecimal> response = restTemplate.exchange(
                    API_BASE_URL + BALANCE_ENDPOINT,
                    HttpMethod.GET,
                    makeAuthEntity(),
                    BigDecimal.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("Failed to get balance. Status code: " + response.getStatusCode());
            }
        } catch (RestClientResponseException e) {
            handleRestClientResponseException(e);
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Cannot access the resource: " + e.getMessage());
        }
        return null;
    }
    public void setCurrentBalance(BigDecimal updatedBalance) {
        this.currentBalance = updatedBalance;
    }

  public Integer getAccountIdByUserId(int userId) {
        Integer accountId = null;
        try {
            ResponseEntity<Integer> response = restTemplate.exchange(
                    ACCOUNT_API_BASE_URL + "/user/" + userId,
                    HttpMethod.GET,
                    makeAuthEntity(),
                    Integer.class
            );
            accountId = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Error fetching account ID: " + e.getMessage());
        }
        return accountId;
  }
    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    private void handleRestClientResponseException(RestClientResponseException e) {
        BasicLogger.log(e.getMessage());
        if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
            System.out.println("Not authorized to perform this operation. Please log in.");
        } else if (e.getRawStatusCode() == HttpStatus.BAD_REQUEST.value()) {
            System.out.println("An error occurred while processing the request: " + e.getStatusText());
        } else {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

}
