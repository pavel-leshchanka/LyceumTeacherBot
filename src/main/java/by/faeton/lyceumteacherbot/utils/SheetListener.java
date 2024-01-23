package by.faeton.lyceumteacherbot.utils;


import by.faeton.lyceumteacherbot.config.BotConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SheetListener {

    private final BotConfig botConfig;

    private static final Logger log = LoggerFactory.getLogger(SheetListener.class);

    private String getSheetJSON(String sheetListName, String fields) {
        String url = String.format("%s%s/values/%s%s?key=%s",
                botConfig.getFirstPart(),
                botConfig.getSheetId(),
                sheetListName,
                fields.equals("") ? "" : "!" + fields,
                botConfig.getApiKey());
        log.info(url + " configured.");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.error("" + e);
            throw new RuntimeException();
        }
        return response.body();
    }

    public Optional<ArrayList<ArrayList<String>>> getSheetList(String sheetListName, String fields) {
        String sheetJSON = getSheetJSON(sheetListName, fields);
        Optional<ArrayList<ArrayList<String>>> list = convertJSONToList(sheetJSON);
        return list;
    }

    public Optional<ArrayList<ArrayList<String>>> getSheetList(String list) {
        return getSheetList(list, "");
    }

    public String getCell(String sheetListName, String field) {
        Optional<ArrayList<ArrayList<String>>> sheetList = getSheetList(sheetListName, field);
        String returnedText = "";
        if (sheetList.isPresent()) {
            returnedText = sheetList.get().get(0).get(0);
        }
        return returnedText;
    }

    private Optional<ArrayList<ArrayList<String>>> convertJSONToList(String text) {
        ArrayList<ArrayList<String>> values;
        try {
            HashMap result = new ObjectMapper().readValue(text, HashMap.class);
            values = (ArrayList<ArrayList<String>>) result.get("values");
        } catch (JsonProcessingException e) {
            log.error("" + e);
            values = null;
        }
        return Optional.ofNullable(values);
    }
}
