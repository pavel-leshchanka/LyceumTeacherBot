package by.faeton.lyceumteacherbot.utils;


import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.config.SheetConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.config.TeleBrowser;
import by.faeton.lyceumteacherbot.controllers.MessageSender;
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
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SheetListener {
    public static final String RAW = "RAW";
    public static final String INSERT_ROWS = "INSERT_ROWS";

    private final SheetConfig sheetConfig;
    private Sheets sheetsService;
    private Credential credential;
    private final MessageSender messageSender;
    private final SheetListNameConfig sheetListNameConfig;
    private final BotConfig botConfig;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String OFFLINE = "offline";
    private static final String RESOURCE_NOT_FOUND = "Resource not found: ";
    private static final String USER = "user";

    private static Credential getCredentials(AuthorizationCodeInstalledApp.Browser browser, final NetHttpTransport HTTP_TRANSPORT, BotConfig botConfig) throws IOException {
        InputStream in = SheetListener.class.getResourceAsStream(botConfig.credentialsFilePath());
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
        Credential authorize = new AuthorizationCodeInstalledApp(flow, receiver, browser).authorize(USER);
        return authorize;
    }

    @PostConstruct
    @SneakyThrows
    public void getSheetsService() {
        TeleBrowser teleBrowser = new TeleBrowser(messageSender);
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        credential = getCredentials(new AuthorizationCodeInstalledApp.DefaultBrowser(), HTTP_TRANSPORT, botConfig);
        Sheets build = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(botConfig.botName())
            .build();
        sheetsService = build;
    }


    public Optional<List<List<String>>> getSheetList(String sheetId, String sheetListName, String fields) {
        String s = sheetListName + (fields.isEmpty() ? "" : "!") + fields;
        List<String> ranges = List.of(s);
        BatchGetValuesResponse readResult = null;
        try {
            Thread.sleep(1000);
            readResult = sheetsService.spreadsheets()
                .values()
                .batchGet(sheetId)
                .setRanges(ranges)
                .execute();
        } catch (Exception e) {
            File file = new File("tokens/StoredCredential");
            Long expiresInSeconds = credential.getExpiresInSeconds();
            if (expiresInSeconds == null || expiresInSeconds < 0) {
                file.delete();
                getSheetsService();
            }
            return getSheetList(sheetId, sheetListName, fields);
        }
        List<ValueRange> valueRanges = readResult.getValueRanges();
        List<List<String>> list = null;
        int size = valueRanges.get(0).size();
        if (size > 2) {
            list = new ArrayList<>(valueRanges.get(0).getValues().stream()
                .map(lists -> lists.stream()
                    .map(Object::toString)
                    .toList())
                .toList());
        }
        return Optional.ofNullable(list);
    }

    public Optional<List<List<String>>> getSheetList(String sheetId, String sheetListName) {
        return getSheetList(sheetId, sheetListName, "");
    }

    @Async
    public void writeSheet(String sheetId, List<ValueRange> data) {
        try {
            BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption(RAW)
                .setData(data);
            sheetsService.spreadsheets()
                .values()
                .batchUpdate(sheetId, body)
                .execute();
        } catch (IOException e) {
            log.warn(e + "data is not written");
        }
    }

    @Async
    public void writeLog(List<List<Object>> content) {
        ValueRange body = new ValueRange()
            .setValues(content);
        try {
            sheetsService.spreadsheets()
                .values()
                .append(sheetConfig.sheetId(), sheetListNameConfig.logsList(), body)
                .setValueInputOption(RAW)
                .setInsertDataOption(INSERT_ROWS)
                .setIncludeValuesInResponse(true)
                .execute();
        } catch (IOException e) {
            log.warn(e + "data is not written");
        }
    }

    @Async
    public void writeNewUser(List<List<Object>> content) {
        ValueRange body = new ValueRange()
            .setValues(content);
        try {

            sheetsService.spreadsheets()
                .values()
                .append(sheetConfig.sheetId(), "newUser", body)
                .setValueInputOption(RAW)
                .setInsertDataOption(INSERT_ROWS)
                .setIncludeValuesInResponse(true)
                .execute();
        } catch (IOException e) {
            log.warn(e + "data is not written");
        }
    }
}