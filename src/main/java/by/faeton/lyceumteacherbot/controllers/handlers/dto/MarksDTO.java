package by.faeton.lyceumteacherbot.controllers.handlers.dto;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarksDTO implements CommandHandler {
    @JsonProperty("q")
    private String quarter;
    @JsonProperty("s")
    private String subject;

    @Override
    @JsonIgnore
    public DialogType getType() {
        return DialogType.QUARTER_MARKS;
    }
}
