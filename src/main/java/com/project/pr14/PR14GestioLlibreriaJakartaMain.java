package com.project.pr14;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.project.objectes.Llibre;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

/**
 * Classe principal que gestiona la lectura i el processament de fitxers JSON per obtenir dades de llibres
 * utilitzant Jakarta JSON Processing (JSON-P).
 */
public class PR14GestioLlibreriaJakartaMain {

    private final File dataFile;

    /**
     * Constructor de la classe PR14GestioLlibreriaJakartaMain.
     *
     * @param dataFile Fitxer on es troben els llibres.
     */
    public PR14GestioLlibreriaJakartaMain(File dataFile) {
        this.dataFile = dataFile;
    }

    public static void main(String[] args) {
        File dataFile = new File(System.getProperty("user.dir"), "data/pr14" + File.separator + "llibres_input.json");
        PR14GestioLlibreriaJakartaMain app = new PR14GestioLlibreriaJakartaMain(dataFile);
        app.processarFitxer();
    }

    /**
     * Processa el fitxer JSON per carregar, modificar, afegir, esborrar i guardar les dades dels llibres.
     */
    public void processarFitxer() {
        List<Llibre> llibres = carregarLlibres();
        if (llibres != null) {
            modificarAnyPublicacio(llibres, 1, 1995);
            afegirNouLlibre(llibres, new Llibre(4, "Històries de la ciutat", "Miquel Soler", 2022));
            esborrarLlibre(llibres, 2);
            guardarLlibres(llibres);
        }
    }

    /**
     * Carrega els llibres des del fitxer JSON.
     * Si el fitxer no existeix o no conté un JSON vàlid, mostra un missatge d'error i retorna null
     * (no ha de propagar cap excepció).
     *
     * @return Llista de llibres o null si hi ha hagut un error en la lectura.
     */
    public List<Llibre> carregarLlibres() {
        if (!dataFile.exists()) {
            System.err.println("Error: El fitxer no existeix a la ruta " + dataFile.getAbsolutePath());
            return null;
        }
        try (JsonReader reader = Json.createReader(new FileReader(dataFile))) {
            JsonArray jsonArray = reader.readArray();
            List<Llibre> llibres = new ArrayList<>();
            for (JsonValue val : jsonArray) {
                JsonObject obj = val.asJsonObject();
                int id = obj.getInt("id");
                String titol = obj.getString("titol");
                String autor = obj.getString("autor");
                int any = obj.getInt("any");
                llibres.add(new Llibre(id, titol, autor, any));
            }
            return llibres;
        } catch (Exception e) {
            System.err.println("Error de lectura o JSON invàlid: " + e.getMessage());
            return null;
        }
    }

    /**
     * Modifica l'any de publicació d'un llibre amb un id específic.
     *
     * @param llibres Llista de llibres.
     * @param id Identificador del llibre a modificar.
     * @param nouAny Nou any de publicació.
     */
    public void modificarAnyPublicacio(List<Llibre> llibres, int id, int nouAny) {
        for (Llibre llibre : llibres) {
            if (llibre.getId() == id) {
                llibre.setAny(nouAny);
                break;
            }
        }
    }

    /**
     * Afegeix un nou llibre a la llista de llibres.
     *
     * @param llibres Llista de llibres.
     * @param nouLlibre Nou llibre a afegir.
     */
    public void afegirNouLlibre(List<Llibre> llibres, Llibre nouLlibre) {
        llibres.add(nouLlibre);
    }

    /**
     * Esborra un llibre amb un id específic de la llista de llibres.
     *
     * @param llibres Llista de llibres.
     * @param id Identificador del llibre a esborrar.
     */
    public void esborrarLlibre(List<Llibre> llibres, int id) {
        llibres.removeIf(llibre -> llibre.getId() == id);
    }

    /**
     * Guarda la llista de llibres en un fitxer nou anomenat llibres_output_jakarta.json,
     * dins del mateix directori que el fitxer d'entrada.
     *
     * @param llibres Llista de llibres a guardar.
     */
    public void guardarLlibres(List<Llibre> llibres) {
        File outputFile = new File(dataFile.getParentFile(), "llibres_output_jakarta.json");
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Llibre llibre : llibres) {
            JsonObjectBuilder objBuilder = Json.createObjectBuilder()
                    .add("id", llibre.getId())
                    .add("titol", llibre.getTitol())
                    .add("autor", llibre.getAutor())
                    .add("any", llibre.getAny());
            arrayBuilder.add(objBuilder);
        }
        JsonArray jsonArray = arrayBuilder.build();

        try (FileWriter writer = new FileWriter(outputFile)) {
            Json.createWriter(writer).write(jsonArray);
        } catch (IOException e) {
            System.err.println("Error al guardar el fitxer JSON: " + e.getMessage());
        }
    }
}