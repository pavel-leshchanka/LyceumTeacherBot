package by.faeton.lyceumteacherbot.model;

import jakarta.validation.constraints.NotNull;
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
public class User {
    @NotNull
    private Long telegramUserId;

    private String subjectOfEducationId;
    @NotNull
    private String userLastName;
    @NotNull
    private String userFirstName;

    private String userFatherName;
    @NotNull
    private String sex;
    @NotNull
    private UserLevel userLevel;

}
