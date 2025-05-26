package app.monybatch.mony.system.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static <T> List<T> parseData(String json,Class<T> clazz) {
        List<T> objectList = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode itemsNode = root.path("response").path("body").path("items").path("item");

            if (itemsNode.isArray()) {
                // `item`이 배열이면 List<Stock>으로 변환
                objectList = objectMapper.readValue(itemsNode.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            } else {
                // `item`이 단일 객체이면 List에 추가
                T object = objectMapper.readValue(itemsNode.toString(), clazz);
                objectList.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return objectList;
    }

    // totalCount 값을 반환하는 메서드
    public static int getTotalCount(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.path("response").path("body").path("totalCount").asInt();
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // 에러 발생 시 기본값 반환
        }
    }

    public static <T> List<T> parseDataFastApi(String json,Class<T> clazz) {
        List<T> objectList = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode stocksNode = rootNode.get("items");

            if (stocksNode == null || !stocksNode.isArray()) {
                return new ArrayList<>();
            }
            List<T> itemList = new ArrayList<>();
            for (JsonNode node : stocksNode) {
                T item = objectMapper.treeToValue(node, clazz);
                objectList.add(item);
            }

        } catch (Exception e) {
            log.error("JSON변환중 오류 :{}",e.getMessage());
        }


        return objectList;
    }

    public static <T> T parseJson(String json, Class<T> clazz) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
    }

    public static <T> List<T> convert(JSONObject data, String key, Class<T> clazz) {
        try {
            JSONArray arr = data.getJSONArray(key);  // org.json.JSONArray

            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(arr.toString(), type);  // arr.toString() gives a valid JSON array
        } catch (Exception e) {
            throw new RuntimeException("변환 실패", e);
        }
    }
}
