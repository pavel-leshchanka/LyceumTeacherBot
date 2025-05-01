package by.faeton.lyceumteacherbot.model;


import by.faeton.lyceumteacherbot.controllers.DialogType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;


@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DialogAttribute {

    private Long id;

    private Long dialogId;

    private DialogType dialogType;

    private Integer stepOfDialog;

    private ArrayList<String> receivedData;

}
