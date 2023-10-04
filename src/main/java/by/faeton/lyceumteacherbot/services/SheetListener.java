package by.faeton.lyceumteacherbot.services;


import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@AllArgsConstructor
@RequiredArgsConstructor
public class SheetListener {

    @Autowired
    private BotConfig botConfig;

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
        String url = String.format("%s%s/values/%s!%s?key=%s",
                botConfig.getFirstPart(),
                botConfig.getSheetId(),
                list,
                field,
                botConfig.getApiKey());
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

}
