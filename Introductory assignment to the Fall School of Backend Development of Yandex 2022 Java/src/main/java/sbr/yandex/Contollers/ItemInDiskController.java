package sbr.yandex.Contollers;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sbr.yandex.Entities.ItemInDisk;
import sbr.yandex.Services.ItemInDiskService;
import sbr.yandex.Services.Parsing;

@Controller
public class ItemInDiskController {
    @Autowired private Parsing parsing;
    @Autowired private ItemInDiskService service;

    @PostMapping("/imports")
    public ResponseEntity<String> importElements(@RequestBody String result) throws JSONException {
        return parsing.parseJSONArray(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteElements(@PathVariable String id){
        return service.deleteFolderOrFileById(id);
    }

    @GetMapping("/nodes/{id}")
    public ResponseEntity<String> getInfoAboutElementById(@PathVariable String id) throws JSONException {
        String jsonString = service.getInfoOfElementById(id).toString();

        if(!jsonString.isEmpty()){
            return ResponseEntity.status(200)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonString);
        } else {
            return ResponseEntity.status(404).body("There is no entity by this id");
        }
    }

    @GetMapping("/updates")
    public ResponseEntity<String> getUpdateList(@RequestParam("date") String date) throws JSONException {
        if (!ItemInDisk.isValidISO8601(date)) {
            return ResponseEntity.status(400).body("Invalid date format. Please use ISO 8601 format.");
        }
        String jsonString = service.getUpdateListByDate(date).toString();

        if(!jsonString.isEmpty()){
            return ResponseEntity.status(200)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonString);
        } else {
            return ResponseEntity.status(404).body("There is no entity by this id");
        }
    }

    @GetMapping("/node/{id}/history")
    public ResponseEntity<String> getHistoryOfUpdates(@PathVariable String id,
                                                      @RequestParam("dateStart") String dateStart,
                                                      @RequestParam("dateEnd") String dateEnd) throws JSONException {
        JSONObject responseObject = service.getHistoryOfUpdatesByIdAndDates(id, ItemInDisk.convertStringToTime(dateStart), ItemInDisk.convertStringToTime(dateEnd));
        String jsonString = responseObject.toString();

        if(!jsonString.isEmpty()){
            return ResponseEntity.status(200)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonString);
        } else {
            return ResponseEntity.status(404).body("There is no entity by this id");
        }
    }

//    private ResponseEntity<String> getHTTPCode(String jsonString){
//        if(!jsonString.isEmpty()){
//            return ResponseEntity.status(200)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(jsonString);
//        } else {
//            return ResponseEntity.status(404).body("There is no entity by this id");
//        }
//    }

}
