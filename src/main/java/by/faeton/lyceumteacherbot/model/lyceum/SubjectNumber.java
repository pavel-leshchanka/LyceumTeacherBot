package by.faeton.lyceumteacherbot.model.lyceum;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectNumber {
    // @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE)
    // @Column(name = "id", nullable = false)
    private Long id;

    private String valueOfTask;

  //  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  //  @JoinColumn(name = "student_id")
    private Student student;

    public void setValueOfTask(String valueOfTask) {
        this.valueOfTask = valueOfTask;
        this.id = null;
    }
}
