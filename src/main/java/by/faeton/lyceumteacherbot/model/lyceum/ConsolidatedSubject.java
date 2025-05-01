package by.faeton.lyceumteacherbot.model.lyceum;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidatedSubject {
    // @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE)
    // @Column(name = "id", nullable = false)
    private Long id;

   // @ManyToOne
   // @JoinColumn(name = "subject_id")
    private Subject subject;

  //  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<SubjectNumber> subjectNumber;
}
