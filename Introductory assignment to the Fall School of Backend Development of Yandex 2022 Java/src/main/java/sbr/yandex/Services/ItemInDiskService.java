package sbr.yandex.Services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.stereotype.Service;
import sbr.yandex.Entities.ItemInDisk;
import sbr.yandex.Interfaces.ItemInDiskRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    public void insertFolderInDataBase(String type, String id, String parentId, String date) {
        if (parentId.equals("None")) {
            diskRepository.save(new ItemInDisk(id, ItemInDisk.convertStringToTime(date), new ArrayList<>(), parentId, 0, type, "None"));
        } else {
            Optional<ItemInDisk> parentItemOptional = diskRepository.findById(parentId);
            if (parentItemOptional.isPresent()) {
                ItemInDisk parentItem = parentItemOptional.get();
                ItemInDisk newItem = new ItemInDisk(id, ItemInDisk.convertStringToTime(date), new ArrayList<>(), parentId, 0, type, "None");
                diskRepository.save(newItem);

                parentItem.setDate(ItemInDisk.convertStringToTime(date));

                parentItem.getChildren().add(newItem);
                diskRepository.delete(parentItem);
                diskRepository.save(parentItem);
            } else {
                System.err.println("Parent item with ID " + parentId + " not found.");
            }
        }
    }

    //Создание объекта для вставки в дб для файлов.
    public void insertFilesInDataBase(String type, String url, String id, String parentId, long size, String date) {
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
                System.err.println("Parent item with ID " + parentId + " not found.");
            }
        }
    }

    //Удаление папки и ее дочерних папок/файлов.
    public void deleteFolderOrFileById(String id){
        Optional<ItemInDisk> parentItemOptional = diskRepository.findById(id);
        if(parentItemOptional.isPresent()){
            List<ItemInDisk> children = parentItemOptional.get().getChildren();
            for(ItemInDisk item : children){
                deleteFolderOrFileById(item.getId());
            }
            diskRepository.deleteById(id);
            System.out.println(!diskRepository.existsById(id));
        } else {
            throw new NoSuchElementException("There is no entity by this id");
        }
    }

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
                jsonObject.put("children:", "None");
            }

        } else {
            throw new NoSuchElementException("There is no entity by this id");
        }

        return jsonObject;
    }

    public List<ItemInDisk> getAll(){
        return diskRepository.findAll();
    }

    public JSONObject getUpdateListByDate(String date) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime parsedDatetime = LocalDateTime.parse(date, formatter);


        long rightBorder = parsedDatetime.toEpochSecond(ZoneOffset.of("Z"));
        long leftBorder = rightBorder - 86400;

        List<ItemInDisk> list = getAll();

        for(ItemInDisk item : list){
            jsonObject.put("result", checkBoundaries(leftBorder, rightBorder, item));
        }

        return jsonObject;

    }

    private JSONObject checkBoundaries(long leftBorder, long rightBorder, ItemInDisk item) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        long currentTimeOfItem = item.getDate().toEpochSecond(ZoneOffset.of("Z"));
        
        if(leftBorder <= currentTimeOfItem || currentTimeOfItem <= rightBorder){
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
                jsonObject.put("children:", "None");

            }
        }
        return jsonObject;
    }
}
