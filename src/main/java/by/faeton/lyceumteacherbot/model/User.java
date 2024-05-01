package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @NotNull
    private Long telegramUserId;
    @NotNull
    private String classParallel;
    @NotNull
    private String classLetter;
    @NotNull
    private String fieldOfSheetWithUser;
    @NotNull
    private String userName;
    @NotNull
    private String sex;
    @NotNull
    private UserLevel userLevel;

}
