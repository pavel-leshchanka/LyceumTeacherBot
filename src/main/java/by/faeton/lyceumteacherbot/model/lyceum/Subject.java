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
public class Subject {
    // @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE)
    // @Column(name = "id", nullable = false)
    private Long id;

    private String name;

   // @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
   // @JoinColumn(name = "teacher_id")
    private Teacher teacher;

  //  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Task> tasks;
}
