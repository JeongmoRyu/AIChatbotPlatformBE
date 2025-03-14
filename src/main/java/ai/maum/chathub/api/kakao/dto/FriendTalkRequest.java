package ai.maum.chathub.api.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FriendTalkRequest {
    @Schema(description = "메시지타입", example = "FT : 친구톡, FI : 이미지 친구톡, FW : 와이드 이미지 친구톡")
    private String messageType;
    @Schema(description = "발신프로필키", example = "4639bf90a9ee65f9a383247bdb34c1fc2ff12896")
    private String senderKey;
    @Schema(description = "사용자 전화번호 (국가코드(82)를 포함한 전화번호) text(16)", example = "821063324347")
    private String phoneNumber;
    @Schema(description = "발신자 전화번호 text(16)", example = "0212345678")
    private String senderNo;
    @Schema(description = "사용자에게 전달할 Kakao 메시지 text(1000)", example = "안녕하세요")
    private String message;
    @Schema(description = "광고성 메시지 여부", example = "N (default:Y)")
    private String adFlag;

    private Image image;
    private List<Button> button;
}
