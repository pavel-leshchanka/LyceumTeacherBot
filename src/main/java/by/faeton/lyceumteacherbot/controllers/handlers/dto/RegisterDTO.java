package by.faeton.lyceumteacherbot.controllers.handlers.dto;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterDTO implements CommandHandler {
    @JsonProperty("ln")
    private String userLastName;
    @JsonProperty("n")
    private String userFirstName;
    @JsonProperty("fn")
    private String userFatherName;
    @JsonProperty("c")
    private String className;
    @JsonProperty("s")
    private String sex;
    @JsonProperty("l")
    private String userLevel;

    @Override
    @JsonIgnore
    public DialogType getType() {
        return DialogType.REGISTER;
    }
}
