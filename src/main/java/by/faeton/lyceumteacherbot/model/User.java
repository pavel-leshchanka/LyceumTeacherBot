package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long telegramUserId;
    private String listOfGoogleSheet;
    private String fieldOfSheetWithUser;
    private String userName;
    private UserLevel userLevel;

}
