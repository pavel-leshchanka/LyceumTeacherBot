package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@ToString
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
