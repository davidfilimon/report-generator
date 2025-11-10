package com.mycompany.report_generator.services;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class LLMClient {

    private final WebClient webClient;

    public LLMClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1") // endpoint OpenAI
                .defaultHeader("Authorization", "Bearer YOUR_OPENAI_API_KEY")
                .build();
    }

    public String generateReport(String prompt) {
        // request simplificat la OpenAI API
        Mono<String> response = webClient.post()
                .uri("/chat/completions")
                .bodyValue("{\"model\": \"gpt-4\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}")
                .retrieve()
                .bodyToMono(String.class);

        return response.block(); // blochează până primim răspunsul
    }
}
