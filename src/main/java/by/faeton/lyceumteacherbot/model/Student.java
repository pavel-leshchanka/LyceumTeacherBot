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
public class Student {
    @NotNull
    private String studentNumber;
    @NotNull
    private String studentName;
    @NotNull
    private String studentClassNumberAndLetter;

}
