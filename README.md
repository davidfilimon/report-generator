# Smart Medical Report Generator

This is a Spring Boot service designed to automate medical reporting by leveraging Large Language Models (LLMs). The system processes patient observations, clinical signs, and medical history to generate structured diagnostic insights and risk assessments.

---

<img width="1138" height="1008" alt="poza1" src="https://github.com/user-attachments/assets/80cef9b2-1b01-4608-8b8c-6920a655a427" />
<img width="1139" height="753" alt="poza2" src="https://github.com/user-attachments/assets/54950255-f456-4bf0-ba35-4b641948174f" />
<img width="697" height="924" alt="poza3" src="https://github.com/user-attachments/assets/3c5b33b6-dacd-4519-b567-9239a9450ca8" />
<img width="882" height="981" alt="poza4" src="https://github.com/user-attachments/assets/d4851e93-595c-4bf3-8225-511fd826eb87" />
<img width="967" height="789" alt="poza5" src="https://github.com/user-attachments/assets/006370f1-68a9-454c-b4b5-fb6109976517" />

## Key Features

* **AI-Driven Analysis**: Integrates with LLM clients to provide intelligent clinical reasoning based on patient data.
* **Structured Medical Reports**: Automatically generates reports containing potential diagnosis, risk leveling (e.g., Low, Medium, Critical), and detailed clinical analysis.
* **Automated Data Processing**: Calculates patient metrics, such as age, and formats clinical data into optimized prompts for the AI model.
* **Transactional Integrity**: Ensures database consistency by managing report versions and automatically cleaning up old records for the same observation.
* **Robust Error Handling**: Includes mechanisms to manage API timeouts, JSON parsing errors, and incomplete patient datasets.

---

## Tech Stack

* **Backend**: Java 17+ / Spring Boot 3
* **AI Integration**: LLM Client (REST-based)
* **Data Serialization**: GSON
* **Database**: Spring Data JPA / Hibernate
* **Model Management**: Lombok (Builder pattern)

---

## Data Processing Pipeline

The `LLMReportGenerationService` follows a strict workflow:

1.  **Data Extraction**: Aggregates patient demographics (age, gender), vitals (blood pressure), risk factors (smoking, cholesterol), and symptoms.
2.  **Prompt Engineering**: Constructs a localized prompt optimized for LLM comprehension, requesting a specific JSON schema.
3.  **Validation**: The service validates the AI response for completeness and handles parsing exceptions to prevent system crashes.
4.  **Persistence**: Maps the analysis to an `ObservationReport` entity and saves it to the repository.

---

## Installation and Setup

### Prerequisites
* JDK 17 or higher
* Maven 3.6+

### Configuration
Ensure the `LLMClient` is properly configured with the necessary API keys in your `application.properties` or `application.yml` file.

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
