package by.faeton.lyceumteacherbot.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TelegramUser {

    private Long telegramUserId;
    private String subjectOfEducationId;
    private String userLastName;
    private String userFirstName;
    private String userFatherName;
    private String sex;
    private UserLevel userLevel;

}
