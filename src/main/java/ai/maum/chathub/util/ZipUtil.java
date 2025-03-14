package ai.maum.chathub.util;

import ai.maum.chathub.api.common.BaseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private ZipUtil() {}

    /**
     * 해당 디렉토리 및 하위 디렉토리까지 포함한 특정 패턴이 존재하는 파일들을 압축한다.
     * 압축이 완료된 후 압축 시도한 파일들을 삭제할 수 있는 설정이 가능하다.
     * @param sourcePath 압축할 파일들이 존재하는 디렉토리 경로
     * @param pattern 압축할 파일들의 패턴, 정규식 정의
     * @param zipPath 압축 파일 저장 경로, 압축 파일명(*.zip) 포함 필수
     * @param isDeleted 압축 시도한 파일들을 삭제할 지 여부
     * @author baekgol
     */
    public static void zip(String sourcePath, String pattern, String zipPath, boolean isDeleted) throws Exception {
        if(!zipPath.endsWith(".zip")) throw BaseException.of("압축 파일 확장자가 잘못되었습니다.");

        try(Stream<Path> pathStream = Files.walk(Path.of(sourcePath))) {
            List<Path> paths = pathStream.filter(path -> StringUtil.matches(path.toString(), pattern))
                    .collect(Collectors.toList());

            if(!paths.isEmpty()) {
                processZip(paths, zipPath);
                if(isDeleted) deleteFiles(paths);
            }
            else throw BaseException.of("압축할 파일들이 존재하지 않습니다.");
        }
    }

    /**
     * 목록에 있는 파일들을 압축한다.
     * 압축이 완료된 후 압축 시도한 파일들을 삭제할 수 있는 설정이 가능하다.
     * @param sources 압축할 파일 경로 목록
     * @param zipPath 압축 파일 저장 경로, 압축 파일명(*.zip) 포함 필수
     * @param isDeleted 압축 시도한 파일들을 삭제할 지 여부
     * @author baekgol
     */
    public static void zip(List<String> sources, String zipPath, boolean isDeleted) {
        if(!zipPath.endsWith(".zip")) throw BaseException.of("압축 파일 확장자가 잘못되었습니다.");

        if(!sources.isEmpty()) {
            List<Path> paths = sources.stream().map(Path::of).collect(Collectors.toList());
            processZip(paths, zipPath);
            if(isDeleted) deleteFiles(paths);
        }
        else throw BaseException.of("압축할 파일들이 존재하지 않습니다.");
    }

    /**
     * 압축이 완료된 후 압축 시도한 파일들을 삭제한다.
     * @param paths 압축 후 삭제될 파일 경로 목록
     * @author baekgol
     */
    private static void deleteFiles(List<Path> paths) {
        paths.forEach(path -> {
            try { Files.deleteIfExists(path); }
            catch(IOException ignored) {}
        });
    }

    /**
     * 파일 목록을 통해 압축을 수행한다.
     * @param paths 압축할 파일 경로 목록
     * @param zipPath 압축 파일 저장 경로, 압축 파일명(*.zip) 포함 필수
     * @author baekgol
     */
    private static void processZip(List<Path> paths, String zipPath) {
        File zipDir = new File(zipPath).getParentFile();

        if(zipDir.exists() || zipDir.mkdir()) {
            try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
                paths.forEach(path -> {
                    try(FileInputStream fis  = new FileInputStream(path.toFile())) {
                        zos.putNextEntry(new ZipEntry(path.getFileName().toString()));
                        int len;
                        byte[] buffer = new byte[1024];
                        while((len = fis.read(buffer)) > 0) zos.write(buffer, 0, len);
                        zos.closeEntry();
                    } catch(IOException e) {
                        throw BaseException.of(e);
                    }
                });

                zos.flush();
            } catch(IOException e) {
                throw BaseException.of(e);
            }
        }
    }
}
