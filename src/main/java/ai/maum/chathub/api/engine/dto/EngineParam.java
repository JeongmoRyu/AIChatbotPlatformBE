package ai.maum.chathub.api.engine.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EngineParam {
    private String label;
    private String key;
    private Range range;
    private Boolean mandatory;
    private String value;

    @Getter
    @Setter
    public static class Range { // static 키워드 추가
        private String from;
        private String to;
    }
}

//        "label":"Frequency penalty",
//                "key":"freq_p",
//                "range": {
//                "from": "0.00",
//                "to": "2.00"
//                },
//                "mandatory": true,
//                "value":"0.5"