package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DialogAttribute {
    @Id
    private Long id;

    private DialogTypeStarted dialogTypeStarted;

    private Integer stepOfDialog;

    private ArrayList<String> receivedData;

}
