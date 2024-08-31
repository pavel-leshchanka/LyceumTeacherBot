package by.faeton.lyceumteacherbot.utils;

public class DefaultMessages {

    private DefaultMessages() {
    }

    public static final String START = """
            Привет!
            Для начала работы необходимо выполнить следующие пункты:
            1. Узнать id своего профиля. Сделать это можно через @userinfobot.
            2. Полученный id, фамилию и класс отправить разработчику бота через старосту класса.
            После внесения вашего id в базу, доступ ко всем функциям бота будет открыт.""";
    public static final String HELP = """
            /start - Начало работы.
            /quarter - Четвертные оценки.
            /marks - Получить оценки.
            /laboratory_notebook - Проверить наличие тетради для лабораторных работ.
            /test_notebook - Проверить наличие тетради для контрольных работ.
            /help - Получить помощь""";
    public static final String AVAILABLE = "В наличии";
    public static final String NOT_AVAILABLE = "Нет в наличии";
    public static final String NOT_AUTHORIZER = "Вы не авторизированы";
    public static final String ANOTHER_MESSAGES = "Для начала работы выполни одну из возможных команд";
    public static final String WHAT_SENDING = "Введи сообщение";
    public static final String NO_ACCESS = "Нет доступа";
    public static final String START_ABSENTEEISM = "Начало пропуска";
    public static final String END_ABSENTEEISM = "Конец пропуска";
    public static final String TYPE_OF_ABSENTEEISM = "Тип пропуска";
    public static final String WRITING_IN_PROGRESS = "Выполняется запись";
    public static final String WRITING_IS_COMPLETED = "Запись выполнена";
    public static final String WRITING_IS_NOT_COMPLETED = "Запись не выполнена. Попробуйте еще раз";
    public static final String CLASS_STUDENTS = "Список класса";
    public static final String CANCELED = "Отменено";
    public static final String CANCEL = "Отмена";
    public static final String CLASS_PARALLEL = "Параллель";
    public static final String CLASS_LETTER = "Буква класса";
    public static final String STUDENT_NOT_FOUND_PLEASE_REPEATE = "Ученик не найден, повторите попытку.";
    public static final String START_MARK_ABSENTEEISM = "Начало пропуска: ";
    public static final String END_MARK_ABSENTEEISM = "Конец пропуска: ";
    public static final String SEX = "Пол";
    public static final String ABSENTEEISM_NOT_FOUND = "Пропусков нет";
    public static final String TASK = " урок ";
    public static final String TASKS = " уроки ";
    public static final String DASH = "-";
    public static final String POINT = ". ";
    public static final String REFRESHED = "Обновлено";

}