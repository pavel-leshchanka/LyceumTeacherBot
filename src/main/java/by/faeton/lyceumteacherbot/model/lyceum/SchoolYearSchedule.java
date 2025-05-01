package by.faeton.lyceumteacherbot.model.lyceum;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

//@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolYearSchedule {
    //  @Id
    //  @GeneratedValue(strategy = GenerationType.SEQUENCE)
    //  @Column(name = "id", nullable = false)
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
