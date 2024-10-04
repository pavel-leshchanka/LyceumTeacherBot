package by.faeton.lyceumteacherbot.model.DTO;

import by.faeton.lyceumteacherbot.model.UserLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class UserRegisterDTO {

    private Long telegramUserId;
    private String className;
    private String userLastName;
    private String userFirstName;
    private String userFatherName;
    private String sex;
    private String userLevel;

}
