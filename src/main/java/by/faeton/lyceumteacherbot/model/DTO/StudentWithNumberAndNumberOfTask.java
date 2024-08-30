package by.faeton.lyceumteacherbot.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class StudentWithNumberAndNumberOfTask {
    private String studentName;
    private String number;
    private Integer numberOfTask;
}
