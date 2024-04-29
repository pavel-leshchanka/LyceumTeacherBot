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
    private String classParallel;
    private String classLetter;
    private String fieldOfSheetWithUser;
    private String userName;
    private String sex;
    private UserLevel userLevel;

}
