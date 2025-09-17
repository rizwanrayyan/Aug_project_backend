package com.example.saveetha_ec.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.saveetha_ec.model.GoldPriceApiResponse;
import com.example.saveetha_ec.model.GoldPriceHistory;
import com.example.saveetha_ec.repository.GoldPriceRepository;

@Service
public class GoldPriceService {
	private final String API_KEY = "goldapi-1nx7c9ismfmgh3a1-io"; 
    private final String API_URL = "https://www.goldapi.io/api/XAU/INR";
    @Autowired
    private GoldPriceRepository goldPriceRepository;
    private final RestTemplate restTemplate=new RestTemplate();
    
	@Scheduled(cron="0 5 0 * * ?")
	public void fetchGoldprice() {
		try {
			HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", API_KEY);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make GET request with headers
            ResponseEntity<GoldPriceApiResponse> responseEntity = restTemplate.exchange(
                    API_URL,
                    HttpMethod.GET,
                    entity,
                    GoldPriceApiResponse.class
            );

            GoldPriceApiResponse response = responseEntity.getBody();

            if (response != null && response.getPrice_gram_24k() != null && response.getTimestamp() != null) {
                GoldPriceHistory goldPrice = new GoldPriceHistory();
                goldPrice.setPrice(response.getPrice_gram_24k());
                goldPrice.setTimestamp(response.getTimestamp() * 1000L);
                // Save to DB
                goldPriceRepository.save(goldPrice);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

}
	public List<GoldPriceHistory> get20daysGoldPrice(){
		return goldPriceRepository.findLast20Prices();
	}
	
}
