package com.eventtoimpact.india.service;

import com.eventtoimpact.india.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Service
public class MlRecommendationClient {
    private final RestClient restClient;

    public MlRecommendationClient(
            @Value("${eventtoimpact.ml.base-url:http://127.0.0.1:8001}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1200);
        requestFactory.setReadTimeout(3000);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .build();
    }

    public MlRecommendationResponse recommend(MlRecommendationRequest request) {
        return restClient.post().uri("/recommend").body(request).retrieve().body(MlRecommendationResponse.class);
    }

    public MlFeedbackResponse feedback(MlFeedbackRequest request) {
        return restClient.post().uri("/feedback").body(request).retrieve().body(MlFeedbackResponse.class);
    }

    public ModelCardResponse modelCard() {
        return restClient.get().uri("/model-card").retrieve().body(ModelCardResponse.class);
    }
}
