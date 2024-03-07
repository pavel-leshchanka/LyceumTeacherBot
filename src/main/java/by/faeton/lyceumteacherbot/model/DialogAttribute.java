package by.faeton.lyceumteacherbot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class DialogAttribute {
@Id
    private Long id;

    private DialogStarted dialogStarted;

    private Integer stepOfDialog;

    private String firstMessage;
    private String secondMessage;




}
