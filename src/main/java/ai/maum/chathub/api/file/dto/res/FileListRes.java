package ai.maum.chathub.api.file.dto.res;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
public class FileListRes {
    private Integer totalPage;
    private List<File> files;

    @Getter
    @Setter
    @Builder
    public static class File {
        private BigInteger rowNum;
        private BigInteger fileId;
        private BigInteger chatbotId;
        private String fileName;
        private String createdAt;
        private String userName;
        private BigInteger fileSize;
    }
}
