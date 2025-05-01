package by.faeton.lyceumteacherbot.model.lyceum;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    //  @Id
    //  @GeneratedValue(strategy = GenerationType.SEQUENCE)
    //  @Column(name = "id", nullable = false)
    private Long id;

    private String studentId;

    private String userLastName;

    private String userFirstName;

    private String userFatherName;

    private String sex;
}
