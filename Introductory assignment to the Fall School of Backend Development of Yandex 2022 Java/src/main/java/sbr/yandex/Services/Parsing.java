package sbr.yandex.Services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Parsing {
    @Autowired
    ItemInDiskService diskService;

    public void parseJSONArray(String string) throws JSONException {
        JSONObject jsonObject = new JSONObject(string);
        JSONArray jsonArray = jsonObject.getJSONArray("items");
        for (int i = 0; i < jsonArray.length(); i++) {
            parseJSONObject(jsonArray.getJSONObject(i), jsonObject.get("updateDate").toString());
        }

    }

    public void parseJSONObject(JSONObject jsonObject, String date) throws JSONException {
        if (jsonObject.get("type").equals("FOLDER")) {
            diskService.insertFolderInDataBase(jsonObject.getString("type"), jsonObject.getString("id"), jsonObject.getString("parentId"), date);
        } else if (jsonObject.get("type").equals("FILE")) {
            diskService.insertFilesInDataBase(jsonObject.getString("type"), jsonObject.getString("url"), jsonObject.getString("id"), jsonObject.getString("parentId"), jsonObject.getLong("size"), date);
        }
    }

}
