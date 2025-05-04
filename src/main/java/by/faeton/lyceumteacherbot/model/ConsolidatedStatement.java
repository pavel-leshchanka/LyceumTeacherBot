package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidatedStatement {
    private Long id;
    private List<ConsolidatedSubject> firstQuarterNumbers;
    private List<ConsolidatedSubject> secondQuarterNumbers;
    private List<ConsolidatedSubject> threeQuarterNumbers;
    private List<ConsolidatedSubject> fourQuarterNumbers;
    private List<ConsolidatedSubject> yearNumbers;
    private List<ConsolidatedSubject> examNumbers;
    private List<ConsolidatedSubject> finalNumbers;
}
