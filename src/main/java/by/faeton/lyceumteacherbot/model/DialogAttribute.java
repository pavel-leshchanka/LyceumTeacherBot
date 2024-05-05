package by.faeton.lyceumteacherbot.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

@Entity
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DialogAttribute {
    @Id
    private Long id;

    private DialogTypeStarted dialogTypeStarted;

    private Integer stepOfDialog;

    private ArrayList<String> receivedData;

}
