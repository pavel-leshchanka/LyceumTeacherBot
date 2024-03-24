package by.faeton.lyceumteacherbot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;

@Entity
@Data
public class DialogAttribute {
    @Id
    private Long id;

    private DialogStarted dialogStarted;

    private Integer stepOfDialog;

    private ArrayList<String> receivedData;

}
