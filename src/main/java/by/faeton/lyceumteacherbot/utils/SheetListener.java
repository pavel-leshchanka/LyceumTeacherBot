package by.faeton.lyceumteacherbot.utils;


import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class SheetListener {

    private final BotConfig botConfig;

    public String getStudentMarks(User user) {
        String field = user.getMarksColumn();
        return getString(user.getList(), field);
    }

    public String getStudentLaboratoryNotebook(User user) {
        String field = user.getLaboratoryNotebookColumn();
        return getString(user.getList(), field);
    }

    public String getStudentTestNotebook(User user) {
        String field = user.getTestNotebookColumn();
        return getString(user.getList(), field);
    }

    @SneakyThrows
    private String getString(String list, String field) {
        String url = String.format("%s%s/values/%s%s?key=%s",
                botConfig.getFirstPart(),
                botConfig.getSheetId(),
                list,
                field.equals("") ? "" : "!" + field,
                botConfig.getApiKey());
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getString(String list) {
        return getString(list, "");
    }

    @SneakyThrows
    public static String getLineToString(String sheetText) {
        HashMap<String, Object> result = new ObjectMapper().readValue(sheetText, HashMap.class);
        ArrayList<Object> values = (ArrayList<Object>) result.get("values");
        String returnedText = new String();
        if (values != null) {
            ArrayList<String> sheetLine = (ArrayList<String>) values.get(0);
            for (String s : sheetLine) {
                returnedText = returnedText + s.toString();
            }
        }
        return returnedText;
    }
}
