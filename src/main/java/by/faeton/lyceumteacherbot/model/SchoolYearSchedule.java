package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolYearSchedule {
    private Long id;
    private LocalDate firstQuarterStart;
    private LocalDate firstQuarterEnd;
    private LocalDate secondQuarterStart;
    private LocalDate secondQuarterEnd;
    private LocalDate threeQuarterStart;
    private LocalDate threeQuarterEnd;
    private LocalDate fourQuarterStart;
    private LocalDate fourQuarterEnd;
}
