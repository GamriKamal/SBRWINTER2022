package sbr.yandex.Services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sbr.yandex.Entities.ItemInDisk;
import sbr.yandex.Interfaces.ItemInDiskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ItemInDiskService {
    @Autowired
    ItemInDiskRepository diskRepository;

    //Создание объекта для вставки в дб для папок.
    public ResponseEntity<String> insertFolderInDataBase(String type, String id, String parentId, String date) {
        if (parentId.equals("None")) {
            diskRepository.save(new ItemInDisk(id, ItemInDisk.convertStringToTime(date), new ArrayList<>(), parentId, 0, type, null));
        } else {
            Optional<ItemInDisk> parentItemOptional = diskRepository.findById(parentId);
            if (parentItemOptional.isPresent()){
                ItemInDisk parentItem = parentItemOptional.get();
                ItemInDisk newItem = new ItemInDisk(id, ItemInDisk.convertStringToTime(date), new ArrayList<>(), parentId, 0, type, null);
                diskRepository.save(newItem);

                parentItem.setDate(ItemInDisk.convertStringToTime(date));
                parentItem.getChildren().add(newItem);

                diskRepository.delete(parentItem);
                diskRepository.save(parentItem);
            } else {
                return ResponseEntity.status(404).body("The element not found!");
            }
        }
        return ResponseEntity.ok("The deletion was successful");
    }

    //Создание объекта для вставки в дб для файлов.
    public ResponseEntity<String> insertFilesInDataBase(String type, String url, String id, String parentId, long size, String date) {
        if (parentId.equals("None")) {
            diskRepository.save(new ItemInDisk(id, ItemInDisk.convertStringToTime(date), new ArrayList<>(), parentId, size, type, url));
        } else {
            Optional<ItemInDisk> parentItemOptional = diskRepository.findById(parentId);
            if (parentItemOptional.isPresent()) {
                ItemInDisk parentItem = parentItemOptional.get();
                ItemInDisk newItem = new ItemInDisk(id, ItemInDisk.convertStringToTime(date), new ArrayList<>(), parentId, size, type, url);
                diskRepository.save(newItem);

                parentItem.setSize(parentItem.getSize() + size);
                parentItem.setDate(ItemInDisk.convertStringToTime(date));

                if(!parentItem.getParentId().equals("0")){
                    System.out.println(parentItem.getParentId());
                    Optional<ItemInDisk> anotherParent = diskRepository.findById(parentItem.getParentId());
                    ItemInDisk anotherItem = anotherParent.get();
                    anotherItem.setSize(anotherItem.getSize() + size);
                    anotherItem.setDate(ItemInDisk.convertStringToTime(date));

                    diskRepository.delete(anotherItem);
                    diskRepository.save(anotherItem);
                }

                parentItem.getChildren().add(newItem);
                diskRepository.delete(parentItem);
                diskRepository.save(parentItem);
            } else {
                return ResponseEntity.status(404).body("The element not found!");
            }
        }
        return ResponseEntity.ok("The deletion was successful");
    }

    //Удаление папки и ее дочерних папок/файлов.
    public ResponseEntity<String> deleteFolderOrFileById(String id){
        Optional<ItemInDisk> parentItemOptional = diskRepository.findById(id);
        if(parentItemOptional.isPresent()){
            List<ItemInDisk> children = parentItemOptional.get().getChildren();
            for(ItemInDisk item : children){
                deleteFolderOrFileById(item.getId());
            }
            diskRepository.deleteById(id);
            if(diskRepository.existsById(id)){
                return ResponseEntity.status(400).body("Invalid document layout or input data is incorrect!");
            }
        } else {
            return ResponseEntity.status(404).body("The element not found!");
        }
        return ResponseEntity.ok("The deletion was successful");
    }

    //Поиск нода и его дочерних файлов.
    public JSONObject getInfoOfElementById(String id) throws JSONException {
        Optional<ItemInDisk> parentItemOptional = diskRepository.findById(id);
        JSONObject jsonObject = new JSONObject();
        if(parentItemOptional.isPresent()){
            jsonObject.put("type:", parentItemOptional.get().getType());
            jsonObject.put("id:", parentItemOptional.get().getId());
            jsonObject.put("size:", parentItemOptional.get().getSize());
            jsonObject.put("url:", parentItemOptional.get().getUrl());
            jsonObject.put("parentId:", parentItemOptional.get().getParentId());
            jsonObject.put("date:", parentItemOptional.get().getDate());
            JSONArray jsonArray = new JSONArray();
            if(!parentItemOptional.get().getChildren().isEmpty()){
                List<ItemInDisk> list = parentItemOptional.get().getChildren();
                for(ItemInDisk item : list){
                    jsonArray.put(getInfoOfElementById(item.getId()));
                }
                jsonObject.put("children:", jsonArray);
            } else {
                jsonObject.put("children:", null);
            }

        } else {
            throw new NoSuchElementException("There is no entity by this id");
        }

        return jsonObject;
    }

    //Поиск последних изменений над файлами.
    public JSONObject getUpdateListByDate(String date) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        LocalDateTime rightBorder = LocalDateTime.parse(date, formatter);
        LocalDateTime leftBorder = rightBorder.minusDays(1);

        List<ItemInDisk> list = diskRepository.findAll();

        for(ItemInDisk item : list){
            jsonObject.put("result", checkBoundaries(leftBorder, rightBorder, item));
        }

        return jsonObject;

    }

    //Поиск нода по айди, если нод найден, вернуть историю обновлений над элементом.
    public JSONObject getHistoryOfUpdatesByIdAndDates(String id, LocalDateTime dateStart, LocalDateTime dateEnd) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        Optional<ItemInDisk> item = diskRepository.findById(id);
        LocalDateTime dateItem = item.get().getDate();
        boolean isDateItemBetween = dateItem.isAfter(dateStart) && dateItem.isBefore(dateEnd);

        if(isDateItemBetween){
            jsonObject.put("type:", item.get().getType());
            jsonObject.put("id:", item.get().getId());
            jsonObject.put("size:", item.get().getSize());
            jsonObject.put("url:", item.get().getUrl());
            jsonObject.put("parentId:", item.get().getParentId());
            jsonObject.put("date:", item.get().getDate());
            JSONArray jsonArray = new JSONArray();

            if(!item.get().getChildren().isEmpty()){
                List<ItemInDisk> children = item.get().getChildren();
                for(ItemInDisk childrenItem : children){
                    jsonArray.put(getHistoryOfUpdatesByIdAndDates(childrenItem.getId(), dateStart, dateEnd));
                }
                jsonObject.put("children:", jsonArray);

            } else {
                jsonObject.put("children:", "None");

            }
        }
        return jsonObject;
    }

    //Внутреняя функция для проверки даты элемента.
    private JSONObject checkBoundaries(LocalDateTime leftBorder, LocalDateTime rightBorder, ItemInDisk item) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        LocalDateTime date2 = item.getDate();

        boolean isDate2Between = date2.isAfter(leftBorder) && date2.isBefore(rightBorder);

        if(isDate2Between){
            jsonObject.put("type:", item.getType());
            jsonObject.put("id:", item.getId());
            jsonObject.put("size:", item.getSize());
            jsonObject.put("url:", item.getUrl());
            jsonObject.put("parentId:", item.getParentId());
            jsonObject.put("date:", item.getDate());
            JSONArray jsonArray = new JSONArray();

            if(!item.getChildren().isEmpty()){
                List<ItemInDisk> children = item.getChildren();
                for(ItemInDisk childrenItem : children){
                    jsonArray.put(checkBoundaries(leftBorder, rightBorder, childrenItem));
                }
                jsonObject.put("children:", jsonArray);

            } else {
                jsonObject.put("children:", null);

            }
        }
        return jsonObject;
    }
}
