package ai.maum.chathub.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class JSONUtil {
    public static void removeKey(JSONObject jsonObject, String keyToRemove) {
        // Key를 직접 제거
//        jsonObject.remove(keyToRemove);
        // Key가 단일 값일 때만 제거
        if (jsonObject.has(keyToRemove)) {
            Object value = jsonObject.get(keyToRemove);
            if (!(value instanceof JSONObject) && !(value instanceof JSONArray)) {
                jsonObject.remove(keyToRemove);
            }
        }

        // 내부 JSONObjects와 JSONArrays에 대해 재귀적으로 제거
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                removeKey((JSONObject) value, keyToRemove);
            } else if (value instanceof JSONArray) {
                removeKeyFromArray((JSONArray) value, keyToRemove);
            }
        }
    }

    public static void removeKeyFromArray(JSONArray jsonArray, String keyToRemove) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);

            if (value instanceof JSONObject) {
                removeKey((JSONObject) value, keyToRemove);
            } else if (value instanceof JSONArray) {
                removeKeyFromArray((JSONArray) value, keyToRemove);
            }
        }
    }

    public static String formatJSON(JSONObject jsonObject) {
        return jsonObject.toString(4);
    }

    public static String formatJSON(JSONArray jsonArray) {
        return jsonArray.toString(4);
    }

    public static String formatMap(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject(map);
        return formatJSON(jsonObject);
    }

    public static String formatList(List<?> list) {
        JSONArray jsonArray = new JSONArray(list);
        return formatJSON(jsonArray);
    }

    public static String formatJSONString(String jsonString) {
        Object json = new JSONObject(jsonString);
        if (json instanceof JSONObject) {
            return formatJSON((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return formatJSON((JSONArray) json);
        }
        return jsonString;
    }


}
