package by.faeton.lyceumteacherbot.model.lyceum;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

//@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectSchedule {
    // @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE)
    // @Column(name = "id", nullable = false)
    private Long id;

    private DayOfWeek dayOfWeek;

    private Integer subjectNumber;

    private Integer semester;

  //  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  //  @JoinColumn(name = "subject_id")
    private Subject subject;

}
