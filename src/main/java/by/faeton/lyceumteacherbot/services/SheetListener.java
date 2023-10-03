package by.faeton.lyceumteacherbot.services;


import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Service
@AllArgsConstructor
@RequiredArgsConstructor
public class SheetListener {
    @Autowired
    private BotConfig botConfig;

    @Autowired
    private UserRepository userRepository;

    public String getStudentMarks(String userId) {
        Optional<User> optionalUser = userRepository.get(Integer.parseInt(userId));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String url = String.format("%s%s/values/%s!%s?key=%s",
                    botConfig.getFirstPart(),
                    botConfig.getSheetId(),
                    user.getList(),
                    user.getField(),
                    botConfig.getApiKey());
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            HttpResponse<String> response;

            try {
                response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return response.body();

        }
        return "You are not authorized";

    }
}
