package ai.maum.chathub.api.meta;

import ai.maum.chathub.meta.CodeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode implements CodeInfo {

    // Fxxx : file 관련
    NO_FILE("F001", "파일을 첨부해 주세요.")
    ,FAILE_FILE_DELETE("F002", "파일 삭제에 실패했습니다. 관리자에게 문의해 주세요.")
    ,FAILE_FILE_DELETE_NO_FILE("F003", "파일이 존재하지 않습니다. 다시 확인해 주세요.")
    ;

    private final String code;
    private final String message;
}
