package ai.maum.chathub.api.kakao.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Button {
    private String name;
    private String type;
    private String urlMobile;
    private String urlPc;
    private String schemeAndroid;
    private String schemeIos;

    public Button(String name, String type, String param1, String param2) {
        this.name = name;
        this.type = type;
        switch(type) {
            case("WL"):
                this.urlMobile = param1;
                this.urlPc = param2;
                break;
            case("AL"):
                this.schemeAndroid = param1;
                this.schemeIos = param2;
                break;
            default:
                break;
        }
    }
}
