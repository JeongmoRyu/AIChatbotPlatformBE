package ai.maum.chathub.meta;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 응답 정보
 * 코드 형식은 Fxxx로 순차적으로 부여한다.
 * @author baekgol
 */
@Getter
@RequiredArgsConstructor
public enum ResponseMeta implements CodeInfo {
     SUCCESS("F000", "성공적으로 수행하였습니다.")
    ,FAILURE("F001", "알 수 없는 오류가 발생했습니다.")
    ,UNAUTHORIZED("F002", "인증되지 않은 사용자입니다.")
    ,ACCESS_DENIED("F003", "권한이 존재하지 않습니다.")
    ,FILE_UPLOAD_ERROR("F004", "파일 업로드 중 오류가 발생했습니다.")
    ,FILE_DELETE_ERROR("F005", "파일 삭제 중 오류가 발생했습니다.")
    ,PARAM_WRONG("F006", "요청 파라미터가 존재하지 않거나 잘못되었습니다.")
    ,UNAUTHORIZED_DOCUMENT("F007", "API 문서 접근이 인증되지 않은 사용자입니다.")
    ,WRONG_DOCUMENT_USER("F008", "잘못된 API 문서 사용자입니다.")
    ,ALREADY_LOGIN_DOCUMENT_USER("F009", "이미 로그인한 API 문서 사용자입니다.")
    ,SSE_EMITTER_NOT_EXIST("F010", "존재하지 않는 Emitter입니다.")
    ,SSE_EMITTER_ID_ALREADY_EXIST("F011", "이미 존재하는 ID입니다.")
    ,SSE_EMITTER_CLIENT_ID_ALREADY_EXIST("F012", "이미 존재하는 클라이언트 ID입니다.")
    ,UNAUTHORIZED_ADMIN("F013", "ADMIN 접근이 인증되지 않은 사용자입니다.")
    ,NO_DATA("F014", "데이터가 없습니다.")
    ,UNAUTHORIZED_PERMISSION("EA00", "권한이 없습니다.")
    ,UNAUTHORIZED_DELETE("EA01", "삭제 권한이 없습니다.")
    ,EXIST_USERID("EA02", "동일한 ID의 계정이 존재합니다.")
    ,NOTEXIST_USERID("EA03", "없는 계정입니다.")
    ,UNAUTHORIZED_MODIFY("EA04", "수정 권한이 없습니다.")
    ,UNAUTHORIZED_SUPER_ADMIN("EA05", "관리자 권한이 필요합니다.")
    ,UNAUTHORIZED_DELETE_MYACCOUNT("EA06", "본인계정은 삭제할 수 없습니다.")
    ,LACK_OF_AUTHORITY("EA07", "권한이 부족합니다.")
    ,NOT_EXIST_CHATBOT("EC01", "챗봇이 없습니다.")
    ,PROCESS_EMBEDDING("EC02", "엠베딩중입니다.")
    ,NOT_ACCESSIBLE_CHATBOT("EC03", "권한이 없는 챗봇입니다.")
    ,NO_CHATROOM("EC04", "채팅룸이 없습니다.")
    ,CHAT_ERROR("EC99", "알 수 없는 오류가 발생했습니다.")
    ,NOT_EXIST_FUNCTION("EF01", "펑션이 없습니다.")
    ,FUNCTION_NOT_IN_USE("EF02", "사용중이지 않은 펑션입니다.")
    ,FUNCTION_IN_USE("EF03", "사용중인 펑션입니다.")
    ,RANKER_API_CALL_ERROR("RK01", "API 호출 실패. (랭커 API 호출 오류)")
    ,RANKER_SAVE_HISTORY_ERROR("RK02", "API 호출 실패. (설정값 오류)")
    ,TOKEN_VALIDATE("T01", "유효한 토큰입니다.")
    ,TOKEN_INVALIDATE("T02", "유효하지 않은 토큰입니다.")
    ,INVALID_PASSWORD("UP01", "현재 비밀번호가 일치하지 않습니다. 다시 시도해주세요.")
    ,DUPLICATE_IN_FILE("UR01", "파일 내 중복된 아이디가 있습니다.")
    ,NO_MANDATORY_VALUE("UR02", "필수 입력 값이 누락되었습니다.\n파일을 다시 확인해주세요")
    ,USING_ID_EXIST("UR03", "사용중인 아이디가 포함되어 있습니다.")
    ,LOGIN_FAIL_NOT_EXIST_ID("AA01", "로그인 실패") // 없는 계정
    ,LOGIN_FAIL_DELETED_ID("AA02", "로그인 실패") // 삭제된 계정
    ;


    private final String code;
    private final String message;
}
