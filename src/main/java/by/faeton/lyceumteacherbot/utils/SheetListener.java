package by.faeton.lyceumteacherbot.utils;


import by.faeton.lyceumteacherbot.config.SheetConfig;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SheetListener {

    private final SheetConfig sheetConfig;
    private final Sheets sheetsService;

    private static final Logger log = LoggerFactory.getLogger(SheetListener.class);


    public Optional<ArrayList<ArrayList<String>>> getSheetList(String sheetListName, String fields) {
        String s = sheetListName + (fields.equals("") ? "" : "!") + fields;
        List<String> ranges = Arrays.asList(s);
        BatchGetValuesResponse readResult = null;
        try {
            readResult = sheetsService.spreadsheets().values()
                    .batchGet(sheetConfig.sheetId())
                    .setRanges(ranges)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<ValueRange> valueRanges = readResult.getValueRanges();
        ArrayList<ArrayList<String>> list = null;
        int size = Arrays.asList(valueRanges.get(0).values().toArray()).size();
        if (size > 2) {
            list = (ArrayList) ((ArrayList) Arrays.asList(valueRanges.get(0).values().toArray()).get(2));
        }
        return Optional.ofNullable(list);
    }

    public Optional<ArrayList<ArrayList<String>>> getSheetList(String list) {
        return getSheetList(list, "");
    }

    public String getCell(String sheetListName, String field) {
        Optional<ArrayList<ArrayList<String>>> sheetList = getSheetList(sheetListName, field);
        String returnedText = "";
        if (sheetList.isPresent()) {
            returnedText = sheetList.get().get(0).get(0);
        }
        return returnedText;
    }


    public void writeSheet(String sheetListName, String startField, List<List<Object>> content) {
        ValueRange body = new ValueRange()
                .setValues(content);
        try {
            String s = sheetListName + (startField.equals("") ? "" : "!") + startField;
            UpdateValuesResponse result = sheetsService.spreadsheets().values()
                    .update(sheetConfig.sheetId(), s, body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            log.warn(e + "data is not writen");
        }
    }


}
