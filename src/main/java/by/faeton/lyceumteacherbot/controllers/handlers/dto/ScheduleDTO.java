package by.faeton.lyceumteacherbot.controllers.handlers.dto;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScheduleDTO implements CommandHandler {
    @JsonProperty("p")
    private String classParallel;
    @JsonProperty("l")
    private String classLetter;
    @JsonProperty("s")
    private String semester;
    @JsonProperty("d")
    private String day;

    @Override
    @JsonIgnore
    public DialogType getType() {
        return DialogType.SCHEDULE;
    }
}
