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
        String sheetLine = getSheetJSONLine(user.getList(), user.getMarksColumn());
        String sheetDateLine = getSheetJSONLine(user.getList(), user.getDateColumn());
        String sheetTypeLine = getSheetJSONLine(user.getList(), user.getTypeOfWorkColumn());
        String marks = getMarksLine(sheetDateLine, sheetTypeLine, sheetLine);

        return marks;
    }
    public String getStudentQuarterMarks(User user) {
        String sheetLine = getSheetJSONLine(user.getList(), user.getQuarterMarksColumn());
        String sheetDateLine = getSheetJSONLine(user.getList(), user.getQuarterNameColumn());
        String sheetTypeLine = getSheetJSONLine(user.getList(), user.getTypeOfQuarterColumn());
        String marks = getMarksLine(sheetDateLine, sheetTypeLine, sheetLine);

        return marks;
    }

    public String getStudentLaboratoryNotebook(User user) {
        String field = user.getLaboratoryNotebookColumn();
        String sheetJSONLine = getSheetJSONLine(user.getList(), field);
        return convertSheetJSONLineToString(sheetJSONLine);
    }

    public String getStudentTestNotebook(User user) {
        String field = user.getTestNotebookColumn();
        String sheetJSONLine = getSheetJSONLine(user.getList(), field);
        return convertSheetJSONLineToString(sheetJSONLine);
    }

    @SneakyThrows
    private String getSheetJSONLine(String sheetListName, String fields) {
        String url = String.format("%s%s/values/%s%s?key=%s",
                botConfig.getFirstPart(),
                botConfig.getSheetId(),
                sheetListName,
                fields.equals("") ? "" : "!" + fields, //todo null
                botConfig.getApiKey());
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getSheetJSONLine(String list) {
        return getSheetJSONLine(list, ""); //todo null
    }

    @SneakyThrows
    public static String convertSheetJSONLineToString(String sheetJSONText) {
        ArrayList<Object> values = convertJSONToList(sheetJSONText);
        String returnedText = "";
        if (values != null) {
            ArrayList<String> sheetLine = (ArrayList<String>) values.get(0);
            for (String s : sheetLine) {
                returnedText = returnedText + s.toString();//todo delete toString?
            }
        }
        return returnedText;
    }

    @SneakyThrows
    public static String getMarksLine(String dateJSONText, String typeOfWorkJSONText, String sheetJSONText) {
        ArrayList<Object> dateValues = convertJSONToList(dateJSONText);
        ArrayList<Object> typeValues = convertJSONToList(typeOfWorkJSONText);
        ArrayList<Object> textValues = convertJSONToList(sheetJSONText);
        String returnedText = "";
        if (textValues != null) {
            ArrayList<String> dateLine = (ArrayList<String>) dateValues.get(0);
            ArrayList<String> typeLine = (ArrayList<String>) typeValues.get(0);
            ArrayList<String> textLine = (ArrayList<String>) textValues.get(0);//todo in method
            for (int i = 0; i < textLine.size(); i++) {
                if (textLine.get(i) != null && !textLine.get(i).equals("")) {
                    String date = "";
                    if (dateLine.size() > i && dateLine.get(i) != null) {
                        //todo check it
                        date = dateLine.get(i);
                    }
                    String type = "";
                    if (typeLine.size() > i && typeLine.get(i) != null) {
                        //todo check it
                        type = typeLine.get(i);
                    }
                    returnedText = returnedText + date + " " + type + " " + textLine.get(i) + '\n';
                }
            }
        }
        return returnedText;
    }

    private static ArrayList<Object> convertJSONToList(String text) throws JsonProcessingException {
        HashMap<String, Object> result = new ObjectMapper().readValue(text, HashMap.class);
        ArrayList<Object> values = (ArrayList<Object>) result.get("values");
        return values;//todo returned list?
    }
}
