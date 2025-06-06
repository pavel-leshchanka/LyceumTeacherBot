package by.faeton.lyceumteacherbot.controllers.handlers.dto;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuarterMarksDTO implements CommandHandler {
    @JsonProperty("q")
    private String quarter;

    @Override
    @JsonIgnore
    public DialogType getType() {
        return DialogType.QUARTER_MARKS;
    }
}
