package ai.maum.chathub.api.chatbotInfo;

import ai.maum.chathub.api.admin.entity.ChatMonitorLogEntity;
import ai.maum.chathub.api.admin.service.MonitorLogService;
import ai.maum.chathub.api.chatbotInfo.entity.ChatbotInfoIdEntity;
import ai.maum.chathub.api.chatbotInfo.service.ChatbotInfoService;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.mybatis.vo.ChatbotInfoVO;
import ai.maum.chathub.mybatis.vo.ElasticVO;
import ai.maum.chathub.mybatis.vo.FunctionVO;
import ai.maum.chathub.mybatis.vo.LibraryVO;
import ai.maum.chathub.api.chat.service.FunctionCheckService;
import ai.maum.chathub.api.chatbot.dto.ChatbotContent;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.engine.service.EngineService;
import ai.maum.chathub.api.file.service.ImageFileService;
import ai.maum.chathub.api.member.dto.MemberDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.postgresql.util.PGobject;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name="챗봇정보", description="챗봇관리API V2")
public class ChatbotInfoController {
    private final ChatbotInfoService chatbotInfoService;
    private final ImageFileService imageFileService;
    private final MonitorLogService monitorLogService;
    private final EngineService engineService;
    private final FunctionCheckService functionCheckService;
//    private final MemberService memberService;

    @Operation(summary = "선택항목리스트", description = "챗봇내 선택 항목 목록 (LLM Engine / Function / Library / Embedding Model / Elastic List")
    @ResponseBody
    @GetMapping({"/chatbotinfo/optionlist", "/chatbotinfo/optionlist/{chatbot_id}"})
    public BaseResponse<JsonNode> getOptionList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = false) @Parameter(name = "chatbot_id") Long chatbotId
    ) {

        JsonNode jsonNode = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String,Object> map = chatbotInfoService.getOptionList(user.getUserKey());
            jsonNode = objectMapper.convertValue(map, JsonNode.class);
            return BaseResponse.success(jsonNode);
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure(jsonNode, "오류가 발생했습니다.");
        }
    }

    @Operation(summary = "기본값조회", description = "챗봇 기본값 조회 (LLM Engine / Function / Library / Embedding Model / Elastic List")
    @ResponseBody
    @GetMapping({"/chatbotinfo/default"})
    public BaseResponse<JsonNode> getDefaultValue(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
    ) {
        return getChatbotInfo(user, 0L);
    }

    @Operation(summary = "목록조회", description = "챗봇목록조회")
    @ResponseBody
    @GetMapping({"/chatbotinfo"})
    public BaseResponse<List<Map<String,Object>>> getChatbotInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
    ) {
        List<Map<String,Object>> result = null;
        try {
//            String userId = String.valueOf(user.getUserKey());
//            return BaseResponse.success(chatbotInfoService.getChatbotInfoList(userId));
            result = chatbotInfoService.getChatbotInfoList(user);
            return BaseResponse.success(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure(result, "오류가 발생했습니다.");
        }
    }

    @Operation(summary = "정보조회", description = "챗봇상세정보조회")
    @ResponseBody
    @GetMapping({"/chatbotinfo/{chatbot_id}"})
    public BaseResponse<JsonNode> getChatbotInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = false) @Parameter(name = "chatbot_id") Long chatbotId
            ) {

        JsonNode jsonNode = null;
        //user 소유의 쳇봇인지 여부 체크 필요.
        List<Map<String, Object>> result = chatbotInfoService.getChatbotInfoMap(chatbotId);
        JSONObject jsonRtn = new JSONObject();
        ObjectMapper objectMapper = new ObjectMapper();

        for(Map<String, Object> item:result) {
            String key = (String) item.get("json_key");
            PGobject value = (PGobject) item.get("json_value");

            JSONObject jsonObject = new JSONObject();

            if(value != null)
                jsonObject = new JSONObject(value.getValue());

            if("root".equals(key)) {
                for(String keyOfRoot:jsonObject.keySet())
                    jsonRtn.put(keyOfRoot, jsonObject.get(keyOfRoot));
            } else {
                jsonRtn.put(key, jsonObject);
            }
        }

        log.debug("Json to string:" + jsonRtn.toString());
        String jsonString = jsonRtn.toString();

        try {
            jsonNode = objectMapper.readTree(jsonString);
            return BaseResponse.success(jsonNode);
        } catch (JsonMappingException e) {
            return BaseResponse.failure(jsonNode, "오류가 발생했습니다.");
        } catch (JsonProcessingException e) {
            return BaseResponse.failure(jsonNode, "오류가 발생했습니다.");
        }

    }


    @Operation(summary = "생성", description = "새로운 챗봇 생성")
    @ResponseBody
    @PostMapping(value={"/chatbotinfo"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> insertChatbotInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody ChatbotInfoVO chatbotInfo
    ) {
        try {
            chatbotInfoService.insertChatbotInfo(chatbotInfo, user);
            return BaseResponse.success(null, "성공적으로 수행하였습니다.");
        } catch (Exception e) {
            return BaseResponse.failure(null, "챗봇 생성에 실패하였습니다.");
        }
    }

    @Operation(summary = "수정", description = "챗봇 수정")
    @ResponseBody
    @PutMapping({"/chatbotinfo/{chatbot_id}"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> updateChatbotInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = false) @Parameter(name = "chatbot_id", required = true) Long chatbotId,
            @RequestBody ChatbotInfoVO chatbotInfo
    ) {
        ChatbotInfoIdEntity chatbotInfoIdEntity = chatbotInfoService.getGetbotInfoById(chatbotId);

        if(chatbotInfoIdEntity == null)
            return BaseResponse.failure(ResponseMeta.NOT_EXIST_CHATBOT);
        else {
            //Admin은 모든 챗봇 수정 가능
            //Editor는 내 챗봇만 수정 가능
            if(!user.getIsSuperAdmin() && !user.getIsAdmin()
                    && !chatbotInfoIdEntity.getUserId().equals(String.valueOf(user.getUserKey()))) {
                return BaseResponse.failure(ResponseMeta.UNAUTHORIZED_MODIFY);
            }
//
//            if(user.getIsAdmin() && !user.getIsSuperAdmin()) {
//                String ownerId = chatbotInfoIdEntity.getUserId();
//                String actionId = String.valueOf(user.getUserKey());
//                if(ownerId == null || !ownerId.equals(actionId)) {
//                    return BaseResponse.failure(ResponseMeta.UNAUTHORIZED_MODIFY);
//                }
//            }

            //엠베딩중일 경우는 수정 불가 하도록 함.
            if("P".equals(chatbotInfoIdEntity.getEmbeddingStatus())) {
                return BaseResponse.failure(ResponseMeta.PROCESS_EMBEDDING);
            }
        }

        try {
            chatbotInfoService.updateChatbotInfo(chatbotId, chatbotInfo, user);
            return BaseResponse.success();
        } catch (RuntimeException e) {
            return BaseResponse.failure(e.getMessage());
        } catch (Exception e) {
            return BaseResponse.failure(e.getMessage());
        }
    }

    @Operation(summary = "챗봇이미지조회", description = "챗봇이미지조회")
    @ResponseBody
    @GetMapping({"/chatbotinfo/image/{chatbot_id}"})
    public ResponseEntity<Resource> getChatbotImage(
            @PathVariable(name = "chatbot_id", required = false) @Parameter(name = "chatbot_id") Long chatbotId
    ) {
        ChatbotInfoIdEntity chatbotInfoIdEntity = chatbotInfoService.getGetbotInfoById(chatbotId);
        if(chatbotInfoIdEntity != null && chatbotInfoIdEntity.getImgfileId() != null)
            return imageFileService.getImage(chatbotInfoIdEntity.getImgfileId().intValue());
        else
            return imageFileService.getDefaultImage();

//        else if(chatbotInfoIdEntity == null || chatbotInfoIdEntity.getName() == null || chatbotInfoIdEntity.getName().isBlank())
//            return imageFileService.generateImage("E");
//        else if(chatbotInfoIdEntity.getName() != null && !chatbotInfoIdEntity.getName().isBlank())
//            return imageFileService.generateImage(chatbotInfoIdEntity.getName());
//        else
//            return ResponseEntity.notFound().build();
    }

    @Operation(summary = "function목록", description = "function목록조회")
    @ResponseBody
    @GetMapping({"/function"})
    public BaseResponse<List<FunctionVO>> getFunctionList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
    ) {
        List<FunctionVO> result = null;

        try {
            result = chatbotInfoService.getFunctionList(user.getUserKey());
            return BaseResponse.success(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure(result, "오류가 발생했습니다.");
        }
    }

    @Operation(summary = "function조회", description = "function상세정보조회")
    @ResponseBody
    @GetMapping({"/function/{function_id}"})
    public BaseResponse<FunctionVO> getFunctionInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "function_id", required = false) @Parameter(name = "function_id") Long functionId

    ) {
        FunctionVO result = null;
        try {
            result = chatbotInfoService.getFunctionInfo(functionId);
            return BaseResponse.success(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure(result, "오류가 발생했습니다.");
        }
    }

    @Operation(summary = "function수정", description = "function수정")
    @ResponseBody
    @PutMapping({"/function/{function_id}"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> modifyFunctionInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "function_id", required = false) @Parameter(name = "function_id") Long functionId,
            @RequestBody FunctionVO functionInfo
    ) {

        FunctionVO reqFunction = chatbotInfoService.getFunctionInfo(functionInfo.getId());

        if(reqFunction == null) {
            return BaseResponse.failure(ResponseMeta.NOT_EXIST_FUNCTION);
        } else {
            //Admin은 모든 펑션 수정 가능
            //Editor는 내 펑션만 수정 가능
            if(!user.getIsSuperAdmin() && !user.getIsAdmin()
                    && !Objects.equals(reqFunction.getUserKey(), user.getUserKey())) {
                return BaseResponse.failure(ResponseMeta.UNAUTHORIZED_MODIFY);
            }

            //ROLE_ADMIN은 본인 것만 핸들링 가능
//            if(user.getIsAdmin() && !user.getIsSuperAdmin()) {
//                String ownerId = String.valueOf(reqFunction.getUserKey());
//                String actionId = String.valueOf(user.getUserKey());
//                if(ownerId == null || !ownerId.equals(actionId)) {
//                    return BaseResponse.failure(ResponseMeta.UNAUTHORIZED_MODIFY);
//                }
//            }
        }

        try {
            int rst = chatbotInfoService.updateFunctionInfo(functionInfo);
            log.debug("update function info result:" + rst);
            return BaseResponse.success();
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure("오류가 발생했습니다.");
        }
    }

    @Operation(summary = "function생성", description = "function생성")
    @ResponseBody
    @PostMapping({"/function"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<String> createFunction(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestBody FunctionVO functionInfo
    ) {
        try {
            chatbotInfoService.insertFunctionInfo(user, functionInfo);
            return BaseResponse.success("테스트");
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure("", "오류가 발생했습니다.");
        }
    }

    @Operation(summary = "채팅로그(테스트로그)", description = "채팅로그(테스트로그)")
    @GetMapping({"/testlog/{chatroom_id}/{seq}"})
    public BaseResponse<Map<String, Object>> getChatTestLog(
            @PathVariable(name = "chatroom_id", required = true) @Parameter(name = "chatroom_id") Long roomId,
            @PathVariable(name = "seq", required = true) @Parameter(name = "seq") Long seq
    ) {

        Map<String,Object> rtnObject = new HashMap<String,Object>();

        try {

            List<ChatMonitorLogEntity> logs = monitorLogService.getChatResultLog(roomId, seq);

            //소요시간 기록을 위한 로직
            List<Map<String, Object>> logs2 = new ArrayList<Map<String,Object>>();
            Timestamp preTime = null;
            Long totalTime = 0L;
            for(ChatMonitorLogEntity item:logs) {
                Long diffTime = preTime==null?0:item.getCreatedAt().getTime()-preTime.getTime();
                totalTime += diffTime;
                Map<String,Object> newItem = new HashMap<String,Object>();
                newItem.put("diff", diffTime);
                preTime = item.getCreatedAt();
                newItem.put("log", item.getLog());
                newItem.put("created_at", item.getCreatedAt());
                newItem.put("title", item.getTitle());
                newItem.put("tokens", item.getTokens() == null?-1:item.getTokens());
                logs2.add(newItem);
            }
            rtnObject.put("logs", logs2);
            rtnObject.put("total_time", totalTime);
            return BaseResponse.success(rtnObject);
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure(rtnObject, "오류가 발생했습니다." + e.getMessage());
        }
    }

    @Operation(summary = "elastic 목록", description = "elastic endpoint 목록조회")
    @ResponseBody
    @GetMapping({"/elastic"})
    public BaseResponse<List<ElasticVO>> getElasticEndpointList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
    ) {
        List<ElasticVO> result = null;
        try {
            result = engineService.getElasticEngineList();
            return BaseResponse.success(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure(result, "오류가 발생했습니다.");
        }
    }

    @Operation(summary = "Library 목록", description = "Library 목록조회")
    @ResponseBody
    @GetMapping({"/library"})
    public BaseResponse<List<LibraryVO>> getLibraryList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
    ) {
        List<LibraryVO> result = null;
        try {
            result = engineService.getLibraryList();
            return BaseResponse.success(result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure(result, "오류가 발생했습니다.");
        }
    }

    @Operation(summary = "엠베딩상태조회", description = "엠베딩상태조회(여러건)")
    @ResponseBody
    @GetMapping({"/chatbotinfo/status"})
    public BaseResponse<List<Map<String,Object>>> getEmbeddingStatusList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user
            ,@RequestParam(value = "chatbot_id_list", required = false) String chatbotIds
            ,@RequestBody(required = false) List<Long> chatbotIdList
    ) {

        List<Map<String,Object>> result = null;

        try {
            if(chatbotIds != null) {
                List<Long> tmpChatbotIdList = null;
                try {
                    tmpChatbotIdList = Arrays.stream(chatbotIds.split(","))
                            .map(String::trim)
                            .map(Long::parseLong)
                            .collect(Collectors.toList());
                    chatbotIdList = tmpChatbotIdList;
                } catch (Exception e) {

                }
            }
            result = chatbotInfoService.selectEmbeddingStatusList(chatbotIdList);
            return BaseResponse.success(result);
//            List<Long> idList = List.of(47L);
//            return BaseResponse.success(chatbotInfoService.selectEmbeddingStatusList(idList));
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure(result, "오류가 발생했습니다.");
        }
    }

    @Operation(summary = "엠베딩상태조회", description = "엠베딩상태조회(단건)")
    @ResponseBody
    @GetMapping({"/chatbotinfo/status/{chatbot_id}"})
    public BaseResponse<Map<String,Object>> getEmbeddingStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = true) @Parameter(name = "chatbot_id") Long chatbotId
    ) {
        Map<String,Object> result = null;
        try {
            result = chatbotInfoService.selectEmbeddingStatus(chatbotId);
            return BaseResponse.success(result);
//            List<Long> idList = List.of(47L);
//            return BaseResponse.success(chatbotInfoService.selectEmbeddingStatusList(idList));
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure(result, "오류가 발생했습니다.");
        }
    }

    @Operation(summary = "챗봇삭제", description = "챗봇삭제(use_yn flag N update")
    @ResponseBody
    @DeleteMapping({"/chatbotinfo/{chatbot_id}"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> deleteChatbotInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = true) @Parameter(name = "chatbot_id") Long chatbotId
    ) {

        //권한체크
        ChatbotInfoIdEntity chatbotInfoIdEntity = chatbotInfoService.getGetbotInfoById(chatbotId);
        if(chatbotInfoIdEntity == null)
            return BaseResponse.failure(ResponseMeta.NOT_EXIST_CHATBOT);
        else {
            if(!user.getIsSuperAdmin() && !user.getIsAdmin()
                    && !chatbotInfoIdEntity.getUserId().equals(String.valueOf(user.getUserKey()))) {
                return BaseResponse.failure(ResponseMeta.UNAUTHORIZED_MODIFY);
            }

            //SUPER_ADMIN은 수정 가능, ADMIN은 본인것만
//            if(user.getIsAdmin() && !user.getIsSuperAdmin()) {
//                String ownerId = chatbotInfoIdEntity.getUserId();
//                String actionId = String.valueOf(user.getUserKey());
//                if(ownerId == null || !ownerId.equals(actionId)) {
//                    return BaseResponse.failure(ResponseMeta.UNAUTHORIZED_DELETE);
//                }
//            }
        }

        try {
            int rtn = chatbotInfoService.deleteChatbotInfo(chatbotId);
            if(rtn > 0)
                return BaseResponse.success();
            else
                return BaseResponse.failure(ResponseMeta.NOT_EXIST_CHATBOT);
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure();
        }
    }

    @Operation(summary = "펑션삭제", description = "펑션삭제(use_yn flag N update")
    @ResponseBody
    @DeleteMapping({"/function/{function_id}"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> deleteFunction(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "function_id", required = true) @Parameter(name = "function_id") Long functionId
    ) {
        //권한체크
        FunctionVO functionVO = chatbotInfoService.getFunctionInfo(functionId);

        if(functionVO == null)
            return BaseResponse.failure(ResponseMeta.NOT_EXIST_FUNCTION);
        else {
            if(!user.getIsSuperAdmin() && !user.getIsAdmin()
                    && !Objects.equals(functionVO.getUserKey(), user.getUserKey())) {
                return BaseResponse.failure(ResponseMeta.UNAUTHORIZED_MODIFY);
            }

//            if(user.getIsAdmin() && !user.getIsSuperAdmin()) {
//                String ownerId = String.valueOf(functionVO.getUserKey());
//                String actionId = String.valueOf(user.getUserKey());
//                if(ownerId == null || !ownerId.equals(actionId)) {
//                    return BaseResponse.failure(ResponseMeta.UNAUTHORIZED_DELETE);
//                }
//            }
        }

        try {
            int rtn = chatbotInfoService.deleteFunction(functionId);
            if(rtn > 0)
                return BaseResponse.success();
            else
                return BaseResponse.failure(ResponseMeta.NOT_EXIST_FUNCTION);
        } catch (Exception e) {
            log.error(e.getMessage());
            return BaseResponse.failure();
        }
    }

    @Operation(summary = "챗봇 컨텐트 조회", description = "해당 챗봇의 컨텐트 (Title, Comment, Cards)를 조회한다.")
    @ResponseBody
    @GetMapping("/chatbot/contents/{chatbot_id}")
    public BaseResponse<ChatbotContent> getChatbotContent (
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "chatbot_id", required = true)  @Parameter(description = "챗봇 ID", required = true) Long chatbotId
    ) {
        return BaseResponse.success(chatbotInfoService.getChatBotContentWithinChatbotInfo(chatbotId));
    }

    @Operation(summary = "펑션체크", description = "펑션체크")
    @ResponseBody
    @GetMapping({"/function/check/{function_id}"})
    @PreAuthorize("hasAnyRole('ROLE_EDITOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public BaseResponse<Void> checkFunction(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @RequestParam(name = "msg", required=false) @Parameter(name = "msg") String msg,
            @PathVariable(name = "function_id", required = true) @Parameter(name = "function_id") Long functionId
    ) {
        //권한체크
        FunctionVO functionVO = chatbotInfoService.getFunctionInfo(functionId);

        if(functionVO == null)
            return BaseResponse.failure(ResponseMeta.NOT_EXIST_FUNCTION);

        try {
            return functionCheckService.checkFunctionCall(functionId, msg, user);
        } catch (Exception e) {
            return BaseResponse.failure(ResponseMeta.FAILURE);
//            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "펑션사용여부체크", description = "펑션사용여부체크")
    @GetMapping({"/function/check/inuse/{function_id}"})
    public BaseResponse<Void> checkFunctionDelete(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "function_id", required = true) @Parameter(name = "function_id") Long functionId
    ) {
            boolean bInUse = chatbotInfoService.checkFunctionUsage(functionId);

            if(bInUse)
                return BaseResponse.success(ResponseMeta.FUNCTION_IN_USE);
            else
                return BaseResponse.success(ResponseMeta.FUNCTION_NOT_IN_USE);
    }
}
