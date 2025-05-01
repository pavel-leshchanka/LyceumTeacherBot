package by.faeton.lyceumteacherbot.controllers.handlers.DTO;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AbsenteeismTextDTO implements CommandHandler {
    @JsonProperty("p")
    private String classParallel;
    @JsonProperty("l")
    private String classLetter;

    @Override
    @JsonIgnore
    public DialogType getType() {
        return DialogType.ABSENTEEISM_TEXT;
    }
}
