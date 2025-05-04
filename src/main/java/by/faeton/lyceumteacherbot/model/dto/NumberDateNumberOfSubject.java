package by.faeton.lyceumteacherbot.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
public class NumberDateNumberOfSubject {
    private String number;
    private LocalDate localDate;
    private Integer numberOfSubject;
}
