package ai.maum.chathub.util;

import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.meta.FileMeta;
import ai.maum.chathub.meta.ResponseMeta;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {
    private FileUtil() {}

    /**
     * 하나의 파일을 업로드한다.
     * @param uploadPath 파일 업로드 디렉토리 경로
     * @param file 파일
     * @return 원본 파일명과 임의로 부여된 파일명 정보
     * @author baekgol
     */
    public static FileInfo upload(String uploadPath, MultipartFile file) {
        if(file == null) throw BaseException.of(ResponseMeta.FILE_UPLOAD_ERROR);

        String fileName = file.getOriginalFilename();
        Path path = getSavedPath(uploadPath, fileName);

        try {
            Files.createDirectories(getDirPath(uploadPath));
            file.transferTo(path);
        } catch (IllegalArgumentException | IOException e) {
            try {
                Files.delete(path);
            } catch(IOException ignored) {}

            throw BaseException.of(ResponseMeta.FILE_UPLOAD_ERROR);
        }

        return FileInfo.builder()
                .orgName(file.getOriginalFilename())
                .savedName(fileName)
                .savedPath(path.toString())
                .size(file.getSize())
                .build();
    }

    /**
     * 여러 개의 파일들을 업로드한다.
     * 파일은 임의로 부여된 이름으로 저장된다.
     * @param uploadPath 파일 업로드 디렉토리 경로
     * @param files 파일 목록
     * @return 원본 파일명과 임의로 부여된 파일명 정보가 담긴 목록
     * @author baekgol
     */
    public static List<FileInfo> upload(String uploadPath, List<MultipartFile> files) {
        return upload(uploadPath, files, null);
    }

    public static List<FileInfo> upload(String uploadPath, List<MultipartFile> files, String regexExt) {
        if(files == null || files.isEmpty()) throw BaseException.of(ResponseMeta.FILE_UPLOAD_ERROR);

        List<Path> sucFiles = new ArrayList<>();
        List<FileInfo> res = new ArrayList<>();

        try {
            Files.createDirectories(getDirPath(uploadPath));

            for(MultipartFile file: files) {
                String ext = getFileExtension(file.getOriginalFilename(), true);
                String fileName = UUID.randomUUID() + ext;

                if(checkUploadableFileExt(ext, regexExt) || regexExt == null) {
                    Path path = getSavedPath(uploadPath, fileName);

                    file.transferTo(path);
                    sucFiles.add(path);
                    res.add(FileInfo.builder()
                            .orgName(file.getOriginalFilename())
                            .savedName(fileName)
                            .savedPath(path.toString())
                            .size(file.getSize())
                            .type(file.getContentType())
                            .build());
                }
            }
        } catch (IOException e) {
            if(!sucFiles.isEmpty()) {
                for(Path file: sucFiles) {
                    try {
                        Files.delete(file);
                    } catch(IOException ignored) {}
                }
            }

            throw BaseException.of(ResponseMeta.FILE_UPLOAD_ERROR);
        }

        return res;
    }

    /**
     * 하나의 파일을 삭제한다.
     * @param path 파일 저장 경로
     * @author baekgol
     */
    public static void delete(String path) {
        if(path == null) throw BaseException.of(ResponseMeta.FILE_DELETE_ERROR);

        try {
            Files.delete(Path.of(path));
        } catch(IOException ignored) {}
    }

    public static boolean deleteIfExist(String path) {
        Boolean bResult = false;
        if(path == null) throw BaseException.of(ResponseMeta.FILE_DELETE_ERROR);
        try {
            Files.delete(Path.of(path));
        } catch(IOException ignored) {
        }
        return bResult;
    }

    /**
     * 여러 개의 파일들을 삭제한다.
     * @param paths 파일 저장 경로 목록
     * @author baekgol
     */
    public static void delete(List<String> paths) {
        if(paths == null || paths.isEmpty()) throw BaseException.of(ResponseMeta.FILE_DELETE_ERROR);

        List<Path> targets = paths.stream().map(Path::of).collect(Collectors.toList());

        for(Path target: targets) {
            try {
                Files.delete(target);
            } catch(IOException ignored) {}
        }
    }

    /**
     * 여러 개의 파일들과 파일들이 속한 디렉토리를 삭제한다.
     * 디렉토리에 파일이 하나라도 존재한다면 디렉토리는 삭제되지 않는다.
     * @param paths 파일 저장 경로 목록
     * @author baekgol
     */
    public static void deleteWithParentDirectory(List<String> paths) {
        if(paths == null || paths.isEmpty()) throw BaseException.of(ResponseMeta.FILE_DELETE_ERROR);

        List<Path> targets = paths.stream().map(Path::of).collect(Collectors.toList());

        try {
            Set<String> dirInfo = new HashSet<>();

            for(Path target: targets) {
                Files.delete(target);
                dirInfo.add(target.getParent().toString());
            }

            for(String info: dirInfo) Files.delete(Path.of(info));
        } catch(IOException ignored) {}
    }

    /**
     * 해당 디렉토리를 포함한 모든 하위 디렉토리들과 파일들을 삭제한다.
     * @param path 디렉토리 경로
     * @author baekgol
     */
    public static void deleteAll(String path) {
        if(path == null) throw BaseException.of(ResponseMeta.FILE_DELETE_ERROR);

        Path dirPath = getDirPath(path);
        Path parentDirPath = dirPath.getParent();

        try(Stream<Path> list = Files.walk(dirPath);
            Stream<Path> parentList = Files.list(parentDirPath)) {
            list.sorted(Comparator.reverseOrder())
                    .forEach(target -> {
                        try { Files.delete(target); }
                        catch(IOException ignored) {}
                    });
            if(parentList.findAny().isEmpty()) Files.delete(parentDirPath);
        } catch(IOException ignored) {}
    }

    /**
     * 파일 확장자를 불러온다.
     * 파일 확장자를 알 수 없을 경우 null을 반환한다.
     * @param file 파일
     * @param hasDot 파일 확장자의 .을 포함할 것인지 여부
     * @return 파일 확장자
     * @author baekgol
     */
    public static String getFileExtension(MultipartFile file, boolean hasDot) {
        return parseForExtension(file.getOriginalFilename(), hasDot);
    }

    /**
     * 파일 확장자를 불러온다.
     * 파일 확장자를 알 수 없을 경우 null을 반환한다.
     * @param fileName 파일명
     * @param hasDot 파일 확장자의 .을 포함할 것인지 여부
     * @return 파일 확장자
     * @author baekgol
     */
    public static String getFileExtension(String fileName, boolean hasDot) {
        return parseForExtension(fileName, hasDot);
    }

    /**
     * 파일 종류를 불러온다.
     * 파일 종류를 알 수 없을 경우 null을 반환한다.
     * @param file 파일
     * @return 파일 종류
     * @author baekgol
     */
    public static String getFileType(MultipartFile file) {
        String ext = getFileExtension(file, false);
        if(ext == null) return null;
        return checkFileType(ext);
    }

    /**
     * 파일 종류를 불러온다.
     * 파일 종류를 알 수 없을 경우 null을 반환한다.
     * @param fileName 파일명
     * @return 파일 종류
     * @author baekgol
     */
    public static String getFileType(String fileName) {
        String ext = getFileExtension(fileName, false);
        if (ext == null) return null;
        return checkFileType(ext);
    }

    /**
     * MultipartFile을 Resource로 변환한다.
     * Resource 사용 후 반드시 물리적으로 저장된 파일을 삭제해야한다.
     * @param file 파일
     * @return Resource 객체
     * @author baekgol
     */
    public static Resource convertToResource(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if(fileName == null) throw new IOException();
        Path tempPath = Files.createTempDirectory(fileName).resolve(fileName);
        Files.write(tempPath, file.getBytes());
        return new FileSystemResource(tempPath.toFile());
    }

    /**
     * 바이너리 데이터를 Resource로 변환한다.
     * Resource 사용 후 반드시 물리적으로 저장된 파일을 삭제해야한다.
     * @param fileName 파일명
     * @param bytes 바이너리 데이터
     * @return Resource 객체
     * @author baekgol
     */
    public static Resource convertToResource(String fileName, byte[] bytes) throws IOException {
        Path tempPath = Files.createTempDirectory(fileName).resolve(fileName);
        Files.write(tempPath, bytes);
        return new FileSystemResource(tempPath.toFile());
    }

    public static Path getPathForEncoding(String path, String fileName) {
        String uriInfo = "file:///";

        if(path.startsWith("file:///"))
            uriInfo += URLEncoder.encode(path.replaceFirst("file:///", "")
                    + (fileName == null ? "" : ("/" + fileName)), StandardCharsets.UTF_8);
        else if(path.startsWith("/"))
            uriInfo += URLEncoder.encode(path.replaceFirst("/", "")
                    + (fileName == null ? "" : ("/" + fileName)), StandardCharsets.UTF_8);
        else
            uriInfo += URLEncoder.encode(path
                    + (fileName == null ? "" : ("/" + fileName)), StandardCharsets.UTF_8);

        return Paths.get(URI.create(uriInfo));
    }

    private static String checkFileType(String ext) {
        switch(ext) {
            case "jpg":
                return FileMeta.TYPE_IMAGE.getCode();
            case "mp4":
                return FileMeta.TYPE_VIDEO.getCode();
            case "wav":
                return FileMeta.TYPE_AUDIO.getCode();
            case "txt":
                return FileMeta.TYPE_TEXT.getCode();
            default:
                return null;
        }
    }

    public static String getContentType(String fileName) {
        // 파일 이름에서 확장자 추출
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null) {
            // 확장자가 없는 경우 기본 MIME 타입 설정 (예: 일반 텍스트)
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // 확장자를 소문자로 변환하여 처리
        extension = extension.toLowerCase();

        // 확장자에 따른 Content-Type 설정
        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG_VALUE; // "image/jpeg"
            case "png":
                return MediaType.IMAGE_PNG_VALUE; // "image/png"
            case "gif":
                return MediaType.IMAGE_GIF_VALUE; // "image/gif"
            default:
                // 처리할 수 없는 확장자의 경우 기본 MIME 타입 설정
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    /**
     * 업로드 가능한 확장자인지 확인한다.
     * @param ext
     * @return
     */
    private static boolean checkUploadableFileExt(String ext, String regex) {
        return StringUtil.matches(ext, regex);
    }

    private static String parseForExtension(String fileName, boolean hasDot) {
        if(fileName == null) return null;
        int idx = fileName.lastIndexOf(".");
        if(idx >= 0 && idx < fileName.length() - 1) return fileName.substring(hasDot ? idx : idx + 1);
        return null;
    }

    private static Path getSavedPath(String path, String fileName) {
        return getPathForEncoding(path, fileName);
    }

    private static Path getDirPath(String path) {
        return getPathForEncoding(path, null);
    }

    public static class MultipartFileAdapter implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public MultipartFileAdapter(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @NotNull
        @Override
        public byte[] getBytes() {
            return content;
        }

        @NotNull
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(@NotNull File dest) throws IOException, IllegalStateException {
            try(OutputStream out = new FileOutputStream(dest)) {
                out.write(content);
            }
        }

        @Override
        public void transferTo(@NotNull Path dest) throws IOException, IllegalStateException {
            MultipartFile.super.transferTo(dest);
        }
    }

    @Data
    @Builder
    public static class FileInfo {
        private String orgName;
        private String savedName;
        private String savedPath;
        private long size;
        private String type;
    }
}
