package by.faeton.lyceumteacherbot.model.lyceum;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
//@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    //  @Id
    //  @GeneratedValue(strategy = GenerationType.SEQUENCE)
    //  @Column(name = "id", nullable = false)
    private Long id;
    private String teacherId;

    private String name;

}
