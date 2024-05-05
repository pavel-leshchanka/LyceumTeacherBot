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
public class Student {
    @NotNull
    private String studentNumber;
    @NotNull
    private String studentName;
    @NotNull
    private String studentClassNumberAndLetter;

}
