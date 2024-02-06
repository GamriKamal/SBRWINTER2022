package sbr.yandex.Contollers;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sbr.yandex.Services.ItemInDiskService;
import sbr.yandex.Services.Parsing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Pattern;

@Controller
public class ItemInDiskController {
    @Autowired private Parsing parsing;
    @Autowired private ItemInDiskService service;

    @PostMapping("/imports")
    public void importElements(@RequestBody String result) throws JSONException {
        parsing.parseJSONArray(result);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteElements(@PathVariable String id){
        service.deleteFolderOrFileById(id);
    }

    @GetMapping("/nodes/{id}")
    public ResponseEntity<String> getInfoAboutElementById(@PathVariable String id) throws JSONException {
        JSONObject responseObject = service.getInfoOfElementById(id);
        String jsonString = responseObject.toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonString);
    }

    @GetMapping("/updates")
    public ResponseEntity<String> getUpdateList(@RequestParam("date") String date) throws JSONException {
        if (!isValidISO8601(date)) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use ISO 8601 format.");
        }

        JSONObject responseObject = service.getUpdateListByDate(date);
        String jsonString = responseObject.toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonString);
    }

    private static boolean isValidISO8601(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        try {
            LocalDateTime parsedDatetime = LocalDateTime.parse(date, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
