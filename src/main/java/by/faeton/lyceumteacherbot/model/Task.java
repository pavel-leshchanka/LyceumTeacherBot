package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Long id;
    private String taskId;
    private String themeName;
    private String homeWork;
    private LocalDate date;
    private Integer taskNumber;
    private List<SubjectNumber> subjectNumbers;
}
