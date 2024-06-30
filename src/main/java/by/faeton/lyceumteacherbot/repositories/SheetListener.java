package by.faeton.lyceumteacherbot.repositories;


import by.faeton.lyceumteacherbot.config.SheetConfig;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SheetListener {

    private final SheetConfig sheetConfig;
    private final Sheets sheetsService;

    @Cacheable("lists")
    public Optional<List<List<String>>> getSheetListFromCache(String sheetListName, String fields) {
        return getSheetList(sheetListName, fields);
    }

    public Optional<List<List<String>>> getSheetList(String sheetId, String sheetListName, String fields) {
        String s = sheetListName + (fields.isEmpty() ? "" : "!") + fields;
        List<String> ranges = List.of(s);
        BatchGetValuesResponse readResult = null;
        try {
            readResult = sheetsService.spreadsheets()
                    .values()
                    .batchGet(sheetId)
                    .setRanges(ranges)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<ValueRange> valueRanges = readResult.getValueRanges();
        List<List<String>> list = null;
        int size = valueRanges.getFirst().size();
        if (size > 2) {
            list = new ArrayList<>(valueRanges.getFirst().getValues().stream()
                    .map(lists -> lists.stream()
                            .map(Object::toString)
                            .toList())
                    .toList());
        }
        return Optional.ofNullable(list);
    }

    public Optional<List<List<String>>> getSheetList(String sheetListName, String fields) {
        return getSheetList(sheetConfig.sheetId(), sheetListName, fields);
    }

    public Optional<List<List<String>>> getSheetList(String list) {
        return getSheetList(list, "");
    }

    public String getCell(String sheetListName, String field) {
        Optional<List<List<String>>> sheetList = getSheetList(sheetListName, field);
        String returnedText = "";
        if (sheetList.isPresent()) {
            returnedText = sheetList.get()
                    .getFirst()
                    .getFirst();
        }
        return returnedText;
    }

    public void writeSheet(String sheetListName, String startCell, List<List<Object>> content) {
        ValueRange body = new ValueRange()
                .setValues(content);
        try {
            String s = sheetListName + (startCell.isEmpty() ? "" : "!") + startCell;
            sheetsService.spreadsheets()
                    .values()
                    .update(sheetConfig.sheetId(), s, body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            log.warn(e + "data is not written");
        }
    }

    public void writeLog(List<List<Object>> content) {
        ValueRange body = new ValueRange()
                .setValues(content);
        try {
            String s = "logs!A1";
            sheetsService.spreadsheets()
                    .values()
                    .append(sheetConfig.sheetId(), s, body)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(true)
                    .execute();
        } catch (IOException e) {
            log.warn(e + "data is not written");
        }
    }
}