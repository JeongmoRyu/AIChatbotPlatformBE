package ai.maum.chathub.api.kakao.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Image {
    private String imgUrl;
    private String imgLink;

    public Image(String imgUrl, String imgLink) {
        this.imgUrl = imgUrl;
        this.imgLink = imgLink;
    }

    public Image() {
    }
}
