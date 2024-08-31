package by.faeton.lyceumteacherbot.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class GoogleConfig {
    private static final String OFFLINE = "offline";
    private static final String RESOURCE_NOT_FOUND = "Resource not found: ";
    private static final String USER = "user";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, BotConfig botConfig) throws IOException {
        InputStream in = GoogleConfig.class.getResourceAsStream(botConfig.credentialsFilePath());
        if (in == null) {
            throw new FileNotFoundException(RESOURCE_NOT_FOUND + botConfig.credentialsFilePath());
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(botConfig.tokensFolder())))
                .setAccessType(OFFLINE)
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(botConfig.port()).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(USER);
    }

    @Bean
    public Sheets getSheetsService(BotConfig botConfig) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, botConfig))
                .setApplicationName(botConfig.botName())
                .build();
    }
}