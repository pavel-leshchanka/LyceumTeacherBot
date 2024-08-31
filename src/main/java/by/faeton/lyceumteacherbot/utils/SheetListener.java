package by.faeton.lyceumteacherbot.utils;


import by.faeton.lyceumteacherbot.config.SheetConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SheetListener {
    public static final String RAW = "RAW";
    public static final String INSERT_ROWS = "INSERT_ROWS";

    private final SheetConfig sheetConfig;
    private final Sheets sheetsService;
    private final SheetListNameConfig sheetListNameConfig;

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

    public Optional<List<List<String>>> getSheetList(String sheetId, String sheetListName) {
        return getSheetList(sheetId, sheetListName, "");
    }

    public void writeSheet(String sheetId, List<ValueRange> data) {
        try {
            BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                    .setValueInputOption(RAW)
                    .setData(data);
            sheetsService.spreadsheets()
                    .values()
                    .batchUpdate(sheetId, body)
                    .execute();
        } catch (IOException e) {
            log.warn(e + "data is not written");
        }
    }

    public void writeLog(List<List<Object>> content) {
        ValueRange body = new ValueRange()
                .setValues(content);
        try {

            sheetsService.spreadsheets()
                    .values()
                    .append(sheetConfig.sheetId(), sheetListNameConfig.logsList(), body)
                    .setValueInputOption(RAW)
                    .setInsertDataOption(INSERT_ROWS)
                    .setIncludeValuesInResponse(true)
                    .execute();
        } catch (IOException e) {
            log.warn(e + "data is not written");
        }
    }
}