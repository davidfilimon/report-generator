package com.mycompany.report_generator.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.io.IOException;

/**
 * Client pentru interacțiunea cu modelul Ollama via API REST.
 */
@Component
public class LLMClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String OLLAMA_MODEL = "cardio-model:latest";
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";

    public LLMClient() {
        this.webClient = WebClient.builder()
                // Configurează URL-ul de bază pentru Ollama
                .baseUrl(OLLAMA_BASE_URL)
                .build();
    }


    public String generateReport(String prompt) {
        ObjectNode jsonPayload = objectMapper.createObjectNode();
        jsonPayload.put("model", OLLAMA_MODEL);
        jsonPayload.put("prompt", prompt); // ObjectMapper se ocupă de escaparea corectă a prompt-ului.
        jsonPayload.put("stream", false);

        String payloadString;
        try {
            payloadString = objectMapper.writeValueAsString(jsonPayload);
        } catch (IOException e) {
            System.err.println("Eroare la serializarea payload-ului JSON: " + e.getMessage());
            return "Eroare internă la construirea cererii LLM.";
        }

        // Efectuează cererea către Ollama
        Mono<String> responseMono = webClient.post()
                .uri("/api/generate")
                .header("Content-Type", "application/json")
                .bodyValue(payloadString)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    System.err.println("Eroare la apelul Ollama: Asigură-te că Ollama rulează la " + OLLAMA_BASE_URL + ". Eroare: " + e.getMessage());
                    return Mono.just("Eroare de generare: Serviciul LLM nu este disponibil.");
                });

        // NOTĂ: Block() oprește și așteaptă un răspuns, funcționând în contextul tău @Transactional.
        String rawResponse = responseMono.block();

        // Parsăm răspunsul JSON de la Ollama pentru a extrage doar conținutul generat.
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            // Calea tipică pentru răspunsul text la un call non-stream este root.response
            if (root.has("response")) {
                return root.get("response").asText();
            }
            // Dacă Ollama returnează un mesaj de eroare (care este tot JSON), îl putem afișa.
            if (root.has("error")) {
                System.err.println("Eroare Ollama (Model lipsă/Gresit): " + root.get("error").asText());
            }
            // Dacă nu e formatul așteptat, returnăm întregul răspuns pentru debug
            return "Format răspuns LLM neașteptat. Răspuns brut: " + rawResponse;
        } catch (IOException e) {
            // Această excepție se întâmplă când rawResponse NU este JSON valid (ex: mesajul "Eroare la parsarea...")
            System.err.println("Eroare la parsarea răspunsului Ollama. Verificați dacă modelul Ollama rulează corect: " + e.getMessage());
            return "Eroare la parsarea răspunsului LLM. Răspuns brut: " + rawResponse;
        } catch (Exception e) {
            System.err.println("Excepție neașteptată în LLMClient: " + e.getMessage());
            return "Excepție în procesarea răspunsului LLM.";
        }
    }
}