package ai.maum.chathub.api.common;

import lombok.Data;

@Data
public class AmsBaseResponse<T> {
    private String result;
    private T data;
    private String message;
}
