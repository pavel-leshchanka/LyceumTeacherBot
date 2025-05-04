package by.faeton.lyceumteacherbot.controllers.handlers.dto;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AbsenteeismDTO implements CommandHandler {
    @JsonProperty("p")
    private String classParallel;
    @JsonProperty("l")
    private String classLetter;
    @JsonProperty("i")
    private String studentId;
    @JsonProperty("s")
    private String startOfAbsenteeism;
    @JsonProperty("e")
    private String endOfAbsenteeism;
    @JsonProperty("t")
    private String typeOfAbsenteeism;

    @Override
    @JsonIgnore
    public DialogType getType() {
        return DialogType.ABSENTEEISM;
    }
}