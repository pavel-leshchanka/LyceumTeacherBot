package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectSchedule {
    private Long id;
    private DayOfWeek dayOfWeek;
    private Integer subjectNumber;
    private Integer semester;
    private Subject subject;
}
