package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    private Long id;
    private String studentId;
    private String userLastName;
    private String userFirstName;
    private String userFatherName;
    private String sex;
}
