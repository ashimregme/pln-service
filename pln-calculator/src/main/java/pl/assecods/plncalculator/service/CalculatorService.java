package pl.assecods.plncalculator.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.assecods.plncalculator.clients.NLBFetcherClient;
import pl.assecods.plncalculator.dto.ExchangeRateResponse;
import pl.assecods.plncalculator.dto.PLNCostRequest;
import pl.assecods.plncalculator.dto.PLNCostResponse;

@Service
public class CalculatorService {
    private final NLBFetcherClient nlbFetcherClient;

    public CalculatorService(NLBFetcherClient nlbFetcherClient) {
        this.nlbFetcherClient = nlbFetcherClient;
    }

    public PLNCostResponse calculatePLN(PLNCostRequest plnCostRequest) {
        double sum = plnCostRequest.getCurrencies().stream()
                .map(plnCostForeignCurrency -> {
                    ResponseEntity<ExchangeRateResponse> response = nlbFetcherClient.getMidExchangeRate(
                            plnCostForeignCurrency.getCode(),
                            plnCostRequest.getDate()
                    );
                    if(response.getStatusCode() == HttpStatus.OK) {
                        return plnCostForeignCurrency.getAmount() * response.getBody().getRate();
                    }
                    return 0.0;
                }).reduce(Double::sum).orElse(0.0);
        return new PLNCostResponse(sum);
    }
}
