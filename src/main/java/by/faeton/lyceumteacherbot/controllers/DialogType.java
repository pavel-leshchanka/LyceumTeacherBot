package by.faeton.lyceumteacherbot.controllers;

import lombok.Getter;

@Getter
public enum DialogType {

    ABSENTEEISM("/absenteeism", "-0-"),
    ABSENTEEISM_TEXT("/absenteeism_text", "-1-"),
    CANCEL("/cancel", "-2-"),
    HELP("/help", "-3-"),
    MARKS("/marks", "-4-"),
    QUARTER_MARKS("/quarter", "-5-"),
    REFRESH("/refresh", "-6-"),
    REGISTER("/register", "-7-"),
    SEND_MESSAGE("/send_message", "-8-"),
    SCHEDULE("/schedule", "-a-"),
    START("/start", "-9-");

    private final String command;
    private final String prefix;

    DialogType(String command, String prefix) {
        this.command = command;
        this.prefix = prefix;
    }
}
