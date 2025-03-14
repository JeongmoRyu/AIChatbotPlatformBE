package ai.maum.chathub.meta;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 정보
 * @author baekgol
 */
@Getter
@RequiredArgsConstructor
public enum FileMeta {
    TYPE_IMAGE("IMAGE", "사진"),
    TYPE_VIDEO("VIDEO", "영상"),
    TYPE_AUDIO("AUDIO", "음성"),
    TYPE_TEXT("TEXT", "문자");

    private final String code;
    private final String codeName;
}
