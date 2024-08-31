package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.config.SheetConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TypeAndValueOfAbsenteeismRepository {


    private final SheetListener sheetListener;
    private final SheetConfig sheetConfig;
    private final SheetListNameConfig sheetListNameConfig;
    private final FieldsNameConfig fieldsNameConfig;

    private final HashMap<String, String> typeAndValueOfAbsenteeism;

    public Map<String, String> getAllTypeAndValueOfAbsenteeism() {
        return Map.copyOf(typeAndValueOfAbsenteeism);
    }

    public String getValueOfAbsenteeism(String type) {
        return typeAndValueOfAbsenteeism.get(type);
    }

    @PostConstruct
    public void refreshContext() {
        typeAndValueOfAbsenteeism.clear();
        Optional<List<List<String>>> absenteeism = sheetListener.getSheetList(sheetConfig.sheetId(), sheetListNameConfig.absenteeismType(), fieldsNameConfig.absenteeismType());
        absenteeism.ifPresent(arrayLists -> arrayLists.forEach(arrayList -> typeAndValueOfAbsenteeism.put(arrayList.get(0), arrayList.get(1))));
    }
}
