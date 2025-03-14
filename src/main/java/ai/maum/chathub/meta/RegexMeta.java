package ai.maum.chathub.meta;

/**
 * 정규식 정보
 * @author baekgol
 */
public class RegexMeta {
    public static final String LOG_FILE_PREFIX = ".*(\\/|\\\\)(info(\\/|\\\\)info|err(\\/|\\\\)err)_";
    public static final String LOG_FILE_SUFFIX = "_\\d*.log$";
    public static final String UNNECESSARY_PATHS = "/favicon.ico|/error";
    public static final String DOCUMENT_PATHS = "/doc|/swagger-ui/.*|/v3/api-docs.*";
    public static final String DOCUMENT_LOGIN_PATHS = "/doc/login(/.*)?";
    public static final String RESOURCE_PATHS = "/resource/.*";
    public static final String MULTIPART_DATE_FORMAT = "^\\[.*\\]$";
    public static final String RESPONSE_IGNORE_PATHS = "^/$";
    public static final String WEBSOCKET_PATH = "/websocket(/.*)?|/socket.io(/.*)?";
//    public static final String SSO_PATH = "/login/email4Sync|/sso/.*|/login/regist";
    public static final String LOGIN_PATH = "/login/email4Sync";
    public static final String TOKEN_VALIDATE_PATH = "/login/token/validate";
    public static final String ADMIN_PATH = "/maum-admin/.*";
    public static final String EXTAPI_PATH = "/extapi/.*";
    public static final String KAKAO_SYNC_PATH = "/extapi/kakao/user/.*";
    public static final String IMAGE_PATH = "/chatbotinfo/image/.*|/file/image/.*";

}
