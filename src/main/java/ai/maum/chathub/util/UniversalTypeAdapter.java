package ai.maum.chathub.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class UniversalTypeAdapter implements JsonSerializer<Object>, JsonDeserializer<Object> {

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        // 모든 객체를 그 객체의 toString() 결과를 이용하여 직렬화합니다.
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // 이 메서드는 역직렬화 시 필요에 따라 사용자 정의 클래스에 대한 적절한 생성자나 팩토리 메서드를 호출해야 합니다.
        // 여기서는 구현이 생략되었으멈, 실제 사용 시 필요한 로직을 구현해야 합니다.
        throw new UnsupportedOperationException("UniversalTypeAdapter.deserialize() is not implemented");
    }

    public static void main(String[] args) {
        Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Object.class, new UniversalTypeAdapter())
                .create();

        // 예시 객체
        MyClass obj = new MyClass("Example");

        // 객체를 JSON 문자열로 직렬화
        String json = gson.toJson(obj);
        System.out.println(json);

        // JSON 문자열을 객체로 역직렬화
        // MyClass newObj = gson.fromJson(json, MyClass.class);
        // System.out.println(newObj);
    }

    static class MyClass {
        private String name;

        public MyClass(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "MyClass{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}

