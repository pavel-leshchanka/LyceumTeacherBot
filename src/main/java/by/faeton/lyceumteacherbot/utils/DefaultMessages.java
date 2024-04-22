package by.faeton.lyceumteacherbot.utils;

public class DefaultMessages {

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
    public static final String WHAT_SENDING = "Что отправляем?";
    public static final String NO_ACCESS = "Нет доступа";
    public static final String START_ABSENTEEISM = "Начало пропуска";
    public static final String END_ABSENTEEISM = "Конец пропуска";
    public static final String TYPE_OF_ABSENTEEISM = "Тип пропуска";
    public static final String WRITING_IN_PROGRESS = "Выполняется запись";
    public static final String WRITING_IS_COMPLETED = "Запись выполнена";
    public static final String WRITING_IS_NOT_COMPLETED = "Запись не выполнена. Попробуйте еще раз";
    public static final String CLASS_STUDENTS = "Список класса";
}