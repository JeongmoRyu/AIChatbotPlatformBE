package ai.maum.chathub.api.file;

import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.file.dto.res.SourceFileSaveRes;
import ai.maum.chathub.api.file.entity.SourceFileEntity;
import ai.maum.chathub.api.file.service.ChatbotFileService;
import ai.maum.chathub.api.file.service.ImageFileService;
import ai.maum.chathub.api.file.service.SourceFileService;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.meta.ResponseCode;
import ai.maum.chathub.api.file.dto.req.ChatbotFileSaveReq;
import ai.maum.chathub.api.file.dto.res.FileListRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Tag(name="파일관리", description="파일관리 API")
public class FileController {
//    @Value("${service.file.s3-doc}")
//    private String FILE_PATH;
//    @Value("${service.file.s3-doc-remote}")
//    private String FILE_PATH_REMOTE;

//    private final Environment env;
//    private List<String> envList;
    private final SourceFileService sourceFileService;
    private final ChatbotFileService chatbotFileService;
    private final HttpServletRequest request;
    private final ImageFileService imageFileService;
//    private final ScpUtil scpUtil;

//    @PostConstruct
//    public void init() {
//        this.envList = Arrays.asList(env.getActiveProfiles());
//    }

    @Operation(summary = "파일 목록", description = "파일 목록 조회")
    @GetMapping
    public BaseResponse<FileListRes> getFileList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            ,@RequestParam(value = "chatbot_id") Integer chatbotId
            ,@RequestParam(value = "page") Integer page
    ) {
        return BaseResponse.success( sourceFileService.findFileList(chatbotId, user.getUsername(), page) );
    }

    @Operation(summary = "파일 사용", description = "파일 사용 저장")
    @PostMapping("/use")
    public BaseResponse<Boolean> setFileUse( @RequestBody ChatbotFileSaveReq param ) {
        return BaseResponse.success(chatbotFileService.deleteAndSave(param));
    }

    @Operation(summary = "파일 삭제", description = "파일 삭제")
    @DeleteMapping("/{file_id}")
    public BaseResponse<Boolean> removeFile(
            @PathVariable(name="file_id") @Parameter(description = "삭제할 File ID", example = "1",required = true) Integer fileId
    ) {
        return BaseResponse.success(sourceFileService.delete(fileId));
    }

    @Operation(summary = "파일/이미지파일 업로드", description = "파일/이미지파일 업로드")
    @PostMapping(path={"", "/", "/image"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<List<SourceFileSaveRes>> uploadFile(
//            HttpRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @ModelAttribute("files") List<MultipartFile> files
    ) {

        String type = "DATA";
        if("/file/image".equals(request.getRequestURI()))
            type = "IMAGE";

        if (files != null && !files.isEmpty() && files.size() > 0) {
            List<SourceFileSaveRes> fileList = sourceFileService.save(user, files, type);
//            List<String> envLIst = Arrays.asList(env.getActiveProfiles());
//            if(fileList != null && fileList.size() > 0 && (envList.contains("local") || envList.contains("local2")) && "DATA".equals(type)) {
//                for(SourceFileSaveRes fileRes:fileList) {
//                    try {
//                        scpUtil.transferFile("minds", "msl123!@#", "183.110.62.148", FILE_PATH_REMOTE, FILE_PATH + "/" + fileRes.getName());
//                    } catch (Exception e) {
//                        log.error(e.getMessage());
//                    }
//                }
//            }
            return  BaseResponse.success(fileList);
        } else {
            throw BaseException.of(ResponseCode.NO_FILE);
        }
    }

    @Operation(summary = "이미지파일조회", description = "이미지파일조회")
    @GetMapping(path={"/image/{file_id}"})
    public ResponseEntity<Resource> getImageFile(
//            HttpRequest request,
//            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "file_id", required = false) @Parameter(name = "file_id") Long fileId
    ) {
            if(fileId == null)
                return ResponseEntity.notFound().build();
            else
                return  imageFileService.getImage(fileId.intValue());
    }

    @Operation(summary = "Function-파일목록조회", description = "Function-파일목록조회")
    @GetMapping(path={"/function/{function_id}"})
    public BaseResponse<List<SourceFileEntity>> getFunctionFileList(
//            HttpRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "function_id", required = false) @Parameter(name = "function_id") Long functionId
    ) {
        return BaseResponse.success(sourceFileService.getFileListByFunctionId(functionId));
    }
}
