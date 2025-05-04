package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectNumber {
    private Long id;
    private String valueOfTask;
    private Student student;

    public void setValueOfTask(String valueOfTask) {
        this.valueOfTask = valueOfTask;
        this.id = null;
    }
}
