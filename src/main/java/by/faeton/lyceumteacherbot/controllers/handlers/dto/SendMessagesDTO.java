package by.faeton.lyceumteacherbot.controllers.handlers.dto;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SendMessagesDTO implements CommandHandler {
    @JsonProperty("p")
    private String classParallels;
    @JsonProperty("l")
    private String classLetters;
    @JsonProperty("s")
    private String sex;
    @JsonProperty("t")
    private String text;

    @Override
    @JsonIgnore
    public DialogType getType() {
        return DialogType.SEND_MESSAGE;
    }
}
