package by.faeton.lyceumteacherbot.config;

import by.faeton.lyceumteacherbot.services.StudentService;
import by.faeton.lyceumteacherbot.services.UserService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class Config {
    private static final String APPLICATION_NAME = "LyceumTeacher";
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    @Bean
    public UserService setUpUserService(FieldsNameConfig fieldsNameConfig) {
        UserService userService = new UserService();
        userService.setLaboratoryNotebookColumn(fieldsNameConfig.getLaboratoryNotebookColumn());
        userService.setTestNotebookColumn(fieldsNameConfig.getTestNotebookColumn());
        userService.setStartMarksColumn(fieldsNameConfig.getStartMarksColumn());
        userService.setEndMarksColumn(fieldsNameConfig.getEndMarksColumn());
        userService.setDateField(fieldsNameConfig.getDateField());
        userService.setFieldTypeOfWork(fieldsNameConfig.getFieldTypeOfWork());
        userService.setStartQuarterMarksColumn(fieldsNameConfig.getStartQuarterMarksColumn());
        userService.setEndQuarterMarksColumn(fieldsNameConfig.getEndQuarterMarksColumn());
        return userService;
    }

    @Bean
    public StudentService setUpStudentsService() {
        StudentService studentService = new StudentService();
        log.info("User Service is configured");
        return studentService;
    }

    @Bean
    public Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = Config.class.getResourceAsStream("/google-sheets-client-secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));
        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                clientSecrets,
                scopes).setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    @Bean
    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

}
