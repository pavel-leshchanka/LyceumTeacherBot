package by.faeton.lyceumteacherbot.model;


import lombok.*;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    private String studentNumber;
    private String studentName;

}
