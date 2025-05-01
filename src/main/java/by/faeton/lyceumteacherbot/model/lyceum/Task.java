package by.faeton.lyceumteacherbot.model.lyceum;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;

//@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    //  @Id
    //  @GeneratedValue(strategy = GenerationType.SEQUENCE)
    //  @Column(name = "id", nullable = false)
    private Long id;

    private String taskId;
    @NotNull
    private String themeName;
    private String homeWork;
    private LocalDate date;
    private Integer taskNumber;

  //  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<SubjectNumber> subjectNumbers;
}
