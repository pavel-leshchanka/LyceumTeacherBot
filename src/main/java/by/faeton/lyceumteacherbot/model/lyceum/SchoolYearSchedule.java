package by.faeton.lyceumteacherbot.model.lyceum;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
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
