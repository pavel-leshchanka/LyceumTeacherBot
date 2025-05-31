package by.faeton.lyceumteacherbot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DefaultMessages {

    public static final String START = """
        Привет!
        Для начала работы необходимо выполнить следующие пункты:
        1.	Зарегистрироваться через команду /register.
        2.	Дождаться регистрации.
        После внесения вашего id в базу, доступ ко всем функциям бота будет открыт.
        """;
    public static final String HELP = """
        /start - Начало работы
        /marks - Текущие отметки
        /quarter - Четвертные отметки
        /schedule - Расписание
        /register - Зарегистрироваться
        /absenteeism - Внести пропуск
        /absenteeism_text - Вывести текст пропусков
        /refresh - Обновить контекст
        /send_message - Отправить сообщение
        /help - Получить помощь
        """;
    public static final String CREATED_NEW_ABSENTEEISM = "%s внес новый пропуск в ваш класс у ученика: %s";
    public static final String NOT_AVAILABLE = "Нет в наличии";
    public static final String SEMESTR = "Семестр";
    public static final String DAYS = "День:";
    public static final String SUBJECT = "Предмет:";
    public static final String AVERAGE = "Среднее: ";
    public static final String QUARTER = "Четверть";
    public static final String ENTER_NAME = "Введите имя:";
    public static final String ENTER_FATHERNAME = "Введите отчество:";
    public static final String ENTER_SEX = "Введите пол:";
    public static final String ENTER_CLASS = "Введите класс:";
    public static final String ENTER_EDUCATION_ROLE = "Выберете кем являетесь:\"Учитель\", \"Ученик\", \"Родитель\"";
    public static final String WAITING = "Ожидайте.";
    public static final String ENTER_LASTNAME = "Введите фамилию:";
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
    public static final String TOO_MATCH_DIALOG_STARTED = "Диалогов больше одного. Начинаем сначала.";
    public static final String NO_MESSAGE = "No message.";
    public static final String FIRST_QUARTER = "Первая четверть";
    public static final String SECOND_QUARTER = "Вторая четверть";
    public static final String THREE_QUARTER = "Третья четверть";
    public static final String FOUR_QUARTER = "Четвертая четверть";
    public static final String YEAR = "Годовая";
    public static final String EXAM = "Экзамен";
    public static final String FINAL = "Итоговая";
}
