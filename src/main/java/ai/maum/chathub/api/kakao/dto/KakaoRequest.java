package ai.maum.chathub.api.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoRequest {
    public KakaoRequest(Intent intent, UserRequest userRequest, Bot bot, Action action) {
        this.intent = intent;
        this.userRequest = userRequest;
        this.bot = bot;
        this.action = action;
    }

    public KakaoRequest() {
    }

    private Intent intent;
    @JsonProperty("userRequest")
    private UserRequest userRequest;
    private Bot bot;
    private Action action;
    private List<Object> contexts;

    public static class Contexts {
        private List<Object> contexts;

        public Contexts() {
        }

        public Contexts(List<Object> contexts) {
            this.contexts = contexts;
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Intent {
        private String id;
        private String name;
        private Extra extra;

        public Intent() {
        }

        public Intent(String id, String name, Extra extra) {
            this.id = id;
            this.name = name;
            this.extra = extra;
        }

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Extra {

            private Reason reason;

            public Extra() {
            }

            public Extra(Reason reason) {
                this.reason = reason;
            }

            @Getter
            @Setter
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Reason {
                private int code;
                private String message;

                public Reason() {
                }

                public Reason(int code, String message) {
                    this.code = code;
                    this.message = message;
                }
            }
        }
    }
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserRequest {
        private String timezone;
        private Params params;
        private Block block;
        private String utterance;
        private String lang;
        private User user;
        @JsonProperty("callbackUrl")
        private String callbackUrl;

        public UserRequest() {
        }

        public UserRequest(String timezone, Params params, Block block, String utterance, String lang, User user, String callbackUrl) {
            this.timezone = timezone;
            this.params = params;
            this.block = block;
            this.utterance = utterance;
            this.lang = lang;
            this.user = user;
            this.callbackUrl = callbackUrl;
        }

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Params {
            @JsonProperty("ignoreMe")
            private String ignoreMe;
            private String surface;

            public Params() {
            }

            public Params(String ignoreMe, String surface) {
                this.ignoreMe = ignoreMe;
                this.surface = surface;
            }
        }
        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Block {
            private String id;
            private String name;

            public Block() {
            }

            public Block(String id, String name) {
                this.id = id;
                this.name = name;
            }
        }
        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class User  {
            private String id;
            private String type;
            private Properties properties;

            public User() {
            }

            public User(String id, String type, Properties properties) {
                this.id = id;
                this.type = type;
                this.properties = properties;
            }

            @Getter
            @Setter
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Properties {
                @JsonProperty("appUserStatus")
                private String appUserStatus;
                @JsonProperty("app_user_status")
                private String app_user_status;

                @JsonProperty("appUserId")
                private String appUserId;
                @JsonProperty("app_user_id")
                private String app_user_id;

                @JsonProperty("botUserKey")
                private String botUserKey;
                @JsonProperty("bot_user_key")
                private String bot_user_key;

                @JsonProperty("plusfriendUserKey")
                private String plusfriendUserKey;
                @JsonProperty("plusfriend_user_key")
                private String plusfriend_user_key;

                @JsonProperty("isFriend")
                private Boolean isFriend;

                public Properties() {
                }

                public Properties(String appUserStatus, String app_user_status, String appUserId, String app_user_id, String botUserKey, String bot_user_key, String plusfriendUserKey, String plusfriend_user_key, Boolean isFriend) {
                    this.appUserStatus = appUserStatus;
                    this.app_user_status = app_user_status;
                    this.appUserId = appUserId;
                    this.app_user_id = app_user_id;
                    this.botUserKey = botUserKey;
                    this.bot_user_key = bot_user_key;
                    this.plusfriendUserKey = plusfriendUserKey;
                    this.plusfriend_user_key = plusfriend_user_key;
                    this.isFriend = isFriend;
                }
            }
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bot {
        private String id;
        private String name;

        public Bot() {
        }

        public Bot(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Action {
        private String name;
        @JsonProperty("clientExtra")
        private ClientExtra clientExtra;
        private Params params;
        private String id;
        @JsonProperty("detailParams")
        private DetailParams detailParams;

        public Action() {
        }

        public Action(String name, ClientExtra clientExtra, Params params, String id, DetailParams detailParams) {
            this.name = name;
            this.clientExtra = clientExtra;
            this.params = params;
            this.id = id;
            this.detailParams = detailParams;
        }

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Params {
            private String message;

            public Params() {
            }

            public Params(String message) {
                this.message = message;
            }
        }
        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DetailParams {
            private Message message;

            public DetailParams() {
            }

            public DetailParams(Message message) {
                this.message = message;
            }

            @Getter
            @Setter
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Message {
                private String origin;
                private String value;
                @JsonProperty("groupName")
                private String groupName;

                public Message() {
                }

                public Message(String origin, String value, String groupName) {
                    this.origin = origin;
                    this.value = value;
                    this.groupName = groupName;
                }
            }
        }

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ClientExtra {
            public ClientExtra() {
            }
        }
    }
}
