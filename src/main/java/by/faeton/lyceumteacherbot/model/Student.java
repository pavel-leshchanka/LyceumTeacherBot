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
public class Student {
    @NotNull
    private String studentNumber;
    @NotNull
    private String studentName;

}
