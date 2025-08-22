package com.hackaton.simulacaocredito.services;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventHubProducerService {

    private final EventHubProducerClient producer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventHubProducerService(@Value("${eventhub.connection-string}") String connectionString, @Value("${eventhub.name}") String eventHubName) {
        this.producer = new EventHubClientBuilder()
                .connectionString(connectionString, eventHubName)
                .buildProducerClient();
    }

    public void enviarSimulacao(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            EventDataBatch batch = producer.createBatch();
            batch.tryAdd(new EventData(json));
            producer.send(batch);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar simulação para Event Hub", e);
        }
    }

    public void close() {
        producer.close();
    }
}
