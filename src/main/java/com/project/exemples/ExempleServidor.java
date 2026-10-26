package com.project.exemples;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.undertow.Undertow;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.Headers;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;

public class ExempleServidor {

    public static void main(String[] args) {
        // Configuració del servidor Undertow
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost") // Escoltar al port 8080
                // Ús de BlockingHandler per permetre I/O bloquejant
                .setHandler(new BlockingHandler(exchange -> {
                    // Obtenir el mètode HTTP i el cos de la petició abans de validar res
                    String metode = exchange.getRequestMethod().toString();
                    String requestBody = "";
                    
                    try {
                        requestBody = new String(exchange.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        requestBody = "";
                    }

                    // --- PAS CLAU PER ALMANACAR LA PETICIÓ EN UN FITXER JSON ---
                    try {
                        LocalDateTime ara = LocalDateTime.now();
                        // Format per al nom del fitxer (evitant caracters invàlids com ':' o espais)
                        String timestampNom = ara.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS"));
                        String timestampJson = ara.toString();

                        // Crear el directori data/requests si no existeix
                        Path directori = Path.of("data", "requests");
                        if (!Files.exists(directori)) {
                            Files.createDirectories(directori);
                        }

                        // Construir l'objecte JSON amb les dades de la petició exigides
                        JsonObject logJson = Json.createObjectBuilder()
                                .add("dataHora", timestampJson)
                                .add("metode", metode)
                                .add("cos", requestBody)
                                .build();

                        // Convertir a String JSON
                        StringWriter sw = new StringWriter();
                        try (JsonWriter jsonWriter = Json.createWriter(sw)) {
                            jsonWriter.write(logJson);
                        }
                        
                        // Guardar a un fitxer diferent cada petició
                        File fitxerSortida = directori.resolve("request_" + timestampNom + ".json").toFile();
                        Files.writeString(fitxerSortida.toPath(), sw.toString(), StandardCharsets.UTF_8);

                    } catch (Exception e) {
                        // Si falla guardar el fitxer, només ho mostrem per consola; el servidor NO ha de fallar pel client
                        System.err.println("Avís: No s'ha pogut guardar la petició al fitxer: " + e.getMessage());
                    }
                    // -----------------------------------------------------------

                    // Permetre només peticions POST (o pots adaptar-ho segons necessitis)
                    if (metode.equalsIgnoreCase("POST")) {
                        // Establir l'encapçalament de la resposta
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

                        try {
                            System.out.println("Rebut JSON: " + requestBody);

                            // Convertir el JSON rebut en un objecte JsonObject
                            try (JsonReader jsonReader = Json.createReader(new StringReader(requestBody))) {
                                JsonObject personaJson = jsonReader.readObject();

                                // Recuperar les dades del JSON
                                String nom = personaJson.getString("nom");
                                int anyNaixement = personaJson.getInt("anyNaixement");

                                // Calcular l'any en què la persona complirà 120 anys
                                int any120 = anyNaixement + 120;

                                // Crear la resposta JSON
                                JsonObject responseJson = Json.createObjectBuilder()
                                        .add("nom", nom)
                                        .add("anyComplira120", any120)
                                        .build();

                                // Convertir la resposta JsonObject en String
                                StringWriter stringWriter = new StringWriter();
                                try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                                    jsonWriter.write(responseJson);
                                }
                                String resposta = stringWriter.toString();

                                // Enviar la resposta al client
                                exchange.getResponseSender().send(resposta);
                            }
                        } catch (Exception e) {
                            // En cas d'error de processament lògic, enviar un missatge d'error al client
                            exchange.setStatusCode(500);
                            exchange.getResponseSender().send("{\"error\": \"Error processant la petició.\"}");
                            e.printStackTrace();
                        }
                    } else {
                        // Si no és una petició POST, indicar que el mètode no està permès
                        exchange.setStatusCode(405); // Mètode no permès
                        exchange.getResponseSender().send("{\"error\": \"Només es permeten peticions POST.\"}");
                    }
                }))
                .build();

        // Arrencar el servidor
        server.start();
        System.out.println("Servidor en execució a http://localhost:8080");
    }
}