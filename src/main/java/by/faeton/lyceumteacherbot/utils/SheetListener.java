package by.faeton.lyceumteacherbot.utils;


import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        String sheetLine = getSheetLine(user.getList(), user.getMarksColumn());
        String sheetDateLine = getSheetLine(user.getList(), user.getDateColumn());
        String sheetTypeLine = getSheetLine(user.getList(), user.getTypeOfWorkColumn());
        String marks = getMarksLine(sheetDateLine, sheetTypeLine, sheetLine);

        return marks;
    }

    public String getStudentLaboratoryNotebook(User user) {
        String field = user.getLaboratoryNotebookColumn();
        return getSheetLine(user.getList(), field);
    }

    public String getStudentTestNotebook(User user) {
        String field = user.getTestNotebookColumn();
        return getSheetLine(user.getList(), field);
    }

    @SneakyThrows
    private String getSheetLine(String list, String field) {
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

    public String getSheetLine(String list) {
        return getSheetLine(list, "");
    }

    @SneakyThrows
    public static String convertSheetLineToString(String sheetText) {
        ArrayList<Object> values = getObjects(sheetText);
        String returnedText = new String();
        if (values != null) {
            ArrayList<String> sheetLine = (ArrayList<String>) values.get(0);
            for (String s : sheetLine) {
                returnedText = returnedText + s.toString();
            }
        }
        return returnedText;
    }

    @SneakyThrows
    public static String getMarksLine(String dateText, String typeOfWork, String sheetText) {
        ArrayList<Object> dateValues = getObjects(dateText);
        ArrayList<Object> typeValues = getObjects(typeOfWork);
        ArrayList<Object> textValues = getObjects(sheetText);
        String returnedText = new String();
        if (textValues != null) {
            ArrayList<String> dateLine = (ArrayList<String>) dateValues.get(0);
            ArrayList<String> typeLine = (ArrayList<String>) typeValues.get(0);
            ArrayList<String> textLine = (ArrayList<String>) textValues.get(0);
            for (int i = 0; i < textLine.size(); i++) {
                if (textLine.get(i) != null && !textLine.get(i).equals("")) {
                    String s = "";
                    if (typeLine.size() < i) {
                        s = "";
                    } else {
                        s = typeLine.get(i);
                    }
                    returnedText = returnedText + dateLine.get(i) + " " + s + " " + textLine.get(i) + '\n';
                }
            }
        }
        return returnedText;
    }

    private static ArrayList<Object> getObjects(String text) throws JsonProcessingException {
        HashMap<String, Object> dateResult = new ObjectMapper().readValue(text, HashMap.class);
        ArrayList<Object> values = (ArrayList<Object>) dateResult.get("values");
        return values;
    }
}
