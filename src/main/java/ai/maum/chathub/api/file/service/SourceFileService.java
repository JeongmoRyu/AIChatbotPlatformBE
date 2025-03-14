package ai.maum.chathub.api.file.service;

import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.meta.ResponseCode;
import ai.maum.chathub.util.DateUtil;
import ai.maum.chathub.util.FileUtil;
import ai.maum.chathub.api.file.dto.res.FileListRes;
import ai.maum.chathub.api.file.dto.res.SourceFileSaveRes;
import ai.maum.chathub.api.file.entity.SourceFileEntity;
import ai.maum.chathub.api.file.repo.ChatbotFileRepository;
import ai.maum.chathub.api.file.repo.SourceFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SourceFileService {
    @Value("${service.file.path}")
    private String FILE_PATH;
    @Value("${service.file.img-path}")
    private String IMG_PATH;

    private final SourceFileRepository sourceFileRepository;
    private final ChatbotFileRepository chatbotFileRepository;;


    public List<SourceFileSaveRes> save(MemberDetail user, List<MultipartFile> files, String type) {
//        String regexExt = ".pdf|.xlsx|.xlsm|.xlsb|.txt|.xls|.doc|.docx|.ppt|.pptx|.jpg|.jpeg|.png|.gif|.hwp|.hwpx|.html|.htm|.owpml|.json|.xml"; // 업로드 가능한 확장자
        String regexExt = ".pdf|.xls|.xlsx|.txt"; // 업로드 가능한 확장자
        String path = FILE_PATH;
        if("IMAGE".equals(type)) {
            path = IMG_PATH;
            regexExt = ".png|.jpg|.jpeg|.gif";
        }

        List<SourceFileEntity> file = FileUtil.upload(path, files, regexExt).stream()
                .map( fileInfo -> SourceFileEntity.builder()
                        .userId(user.getUsername())
                        .userName(user.getName())
                        .orgName(fileInfo.getOrgName())
                        .name(fileInfo.getSavedName())
                        .path(fileInfo.getSavedPath())
                        .size(fileInfo.getSize())
                        .type(fileInfo.getType())
                        .build()
                ).collect(Collectors.toList());

        return sourceFileRepository.saveAll(file).stream()
                .map(savedFile -> SourceFileSaveRes.builder()
                        .id(savedFile.getId())
                        .name(savedFile.getName())
                        .userName(savedFile.getUserName())
                        .createdAt(DateUtil.convertToStringByDateTime(savedFile.getCreatedAt()))
                        .path(savedFile.getPath())
                        .size(savedFile.getSize())
                        .orgName(savedFile.getOrgName())
                        .build()
                ).collect(Collectors.toList());
    }

    public FileListRes findFileList(Integer chatbotId, String userId, Integer page) {
        Page<Object[]> list = sourceFileRepository.findFileList(chatbotId, userId, PageRequest.of(page-1, 10));

        List<FileListRes.File> files = new ArrayList<>();
        list.forEach(
            file -> {
                files.add(
                        FileListRes.File.builder()
                            .rowNum((BigInteger) file[0])
                            .fileId((BigInteger) file[1])
                            .fileName((String) file[2])
                            .createdAt((String) file[3])
                            .userName((String) file[4])
                            .chatbotId((BigInteger) file[5])
                            .fileSize(((BigInteger) file[6]))
                        .build()
                );
            }
        );

        return FileListRes.builder()
                .totalPage( list.getTotalPages() )
                .files(files)
                .build();
    }

    @Transactional
    public boolean delete(Integer fileId) {
        try {
            chatbotFileRepository.deleteByFileId(fileId); // chatbot에서 사용 중인 file들 삭제

            Optional<SourceFileEntity> file = sourceFileRepository.findById(fileId);
            if(!file.isEmpty())
                FileUtil.delete( file.get().getPath() );

            sourceFileRepository.deleteById(fileId); // source_file 삭제
        } catch (EmptyResultDataAccessException e) {
            throw BaseException.of(ResponseCode.FAILE_FILE_DELETE_NO_FILE, e);
        } catch (Exception e) {
            throw BaseException.of(ResponseCode.FAILE_FILE_DELETE, e);
        }

        return true;
    }

    public Boolean deleteSourceFileIfExist(Integer fileId) {
        Boolean bResult = false;
        try {
            Optional<SourceFileEntity> file = sourceFileRepository.findById(fileId);
            if(!file.isEmpty() && FileUtil.deleteIfExist(file.get().getPath())) {
                log.debug("file deleting...:{},{}", fileId, file.get().getPath());
                FileUtil.delete(file.get().getPath());
                sourceFileRepository.deleteById(fileId); // source_file 삭제
                log.debug("file delete complete!!!:{},{}", fileId, file.get().getPath());
                bResult = true;
            }
        } catch (EmptyResultDataAccessException e) {
            throw BaseException.of(ResponseCode.FAILE_FILE_DELETE_NO_FILE, e);
        } catch (Exception e) {
            throw BaseException.of(ResponseCode.FAILE_FILE_DELETE, e);
        }
        return bResult;
    }

    public List<SourceFileEntity> getFileListByFunctionId(Long functionId) {
        return sourceFileRepository.findSourceFileEntitiesByFunctionId(functionId);
    }
}
