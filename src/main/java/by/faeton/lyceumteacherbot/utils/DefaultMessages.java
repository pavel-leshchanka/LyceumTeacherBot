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

}
