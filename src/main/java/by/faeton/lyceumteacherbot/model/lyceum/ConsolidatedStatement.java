package by.faeton.lyceumteacherbot.model.lyceum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidatedStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConsolidatedSubject> firstQuarterNumbers;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConsolidatedSubject> secondQuarterNumbers;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConsolidatedSubject> threeQuarterNumbers;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConsolidatedSubject> fourQuarterNumbers;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConsolidatedSubject> yearNumbers;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConsolidatedSubject> examNumbers;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConsolidatedSubject> finalNumbers;

}
