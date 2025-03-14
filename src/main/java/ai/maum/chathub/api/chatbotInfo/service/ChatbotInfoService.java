package ai.maum.chathub.api.chatbotInfo.service;

import ai.maum.chathub.api.member.mapper.MemberMapper;
import ai.maum.chathub.api.member.service.MemberOrganizationService;
import ai.maum.chathub.mybatis.mapper.ChatbotInfoMapper;
import ai.maum.chathub.mybatis.vo.*;
import ai.maum.chathub.util.DateUtil;
import ai.maum.chathub.api.chat.handler.ChatGrpcConnectionHandler;
import ai.maum.chathub.api.chatbot.dto.ChatbotContent;
import ai.maum.chathub.api.chatbot.dto.ChatbotContentCard;
import ai.maum.chathub.api.chatbot.dto.ChatbotContentComment;
import ai.maum.chathub.api.chatbot.dto.ChatbotContentTitle;
import ai.maum.chathub.api.chatbotInfo.entity.ChatbotInfoIdEntity;
import ai.maum.chathub.api.chatbotInfo.repo.ChatbotInfoRepository;
import ai.maum.chathub.api.engine.entity.EngineEntity;
import ai.maum.chathub.api.engine.service.EngineService;
import ai.maum.chathub.api.file.entity.SourceFileEntity;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotInfoService {
    @Value("${service.file.s3-doc}")
    String S3_PATH;
    @Value("${service.redis.chatbotinfo-key}")
    String REDIS_CHATBOTINFO_KEY;

    private final ChatbotInfoMapper chatbotInfoMapper;
    private final ChatbotInfoRepository chatbotInfoRepository;
    private final ChatGrpcConnectionHandler chatGrpcConnectionHandler;
    private final EngineService engineService;
    private final EmbeddingService embeddingService;
    private final MemberOrganizationService memberOrganizationService;
    private final MemberMapper memberMapper;


    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    public List<Map<String, Object>> getChatbotInfoList(String userId) {
        return chatbotInfoMapper.selectChatbotInfoListByUserId(userId);
    }

    private Map<String, Object> getParamForChatbotAndFunctionSelect(Long userKey) {
        Integer roleLevel = memberMapper.getMyRoleLevel(userKey);

        if(roleLevel == null)
            roleLevel = 1;

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("organizationId", memberOrganizationService.getMemberOrganizationId(userKey));
        param.put("userKey", userKey);
        param.put("roleLevel", roleLevel);

        return param;
    }

    public List<Map<String, Object>> getChatbotInfoList(MemberDetail user) {

        Map<String, Object> param = getParamForChatbotAndFunctionSelect(user.getUserKey());
        log.debug("getChatbotInfoList: {}", ObjectMapperUtil.writeValueAsString(param));
        return chatbotInfoMapper.selectChatbotInfoList(param);

        /*
        if(user.getRoles().indexOf("ROLE_SUPER_ADMIN") > 0 )
            return chatbotInfoMapper.selectChatbotInfoListForSuperAdmin(param);
        else if(user.getRoles().indexOf("ROLE_ADMIN") > 0 )
            return chatbotInfoMapper.selectChatbotInfoListForAdmin(param);
        else if(user.getRoles().indexOf("ROLE_EDITOR") > 0 )
            return chatbotInfoMapper.selectChatbotInfoListForEditor(param);
        else
            return chatbotInfoMapper.selectChatbotInfoListForUser(param);
        */
    }

    public List<FunctionVO> getFunctionList(Long userKey) {
//        return chatbotInfoMapper.selectFunctionListByUserId(userId);
        Map<String, Object> param = getParamForChatbotAndFunctionSelect(userKey);
        log.debug("getChatbotInfoList: {}", ObjectMapperUtil.writeValueAsString(param));
        return chatbotInfoMapper.selectFunctionList(param);
    }

    public FunctionVO getFunctionInfo(Long functionId) {
        return chatbotInfoMapper.selectFunctionInfoById(functionId);
    }

    @Transactional
    public int updateFunctionInfo(FunctionVO function) {
        int updateResult = chatbotInfoMapper.updateFunctionInfo(function);
        if(updateResult > 0) {
            //펑션 정보 업데이트 이후에 파일 매핑 정보 세팅
            //먼저 기존 정보를 삭제 후 새로운 정보 추가
            int resetResult = chatbotInfoMapper.resetFunctionFileMapping(function.getId());
            log.debug("resetResult:" + resetResult);

            List<Object> fileList = function.getFileList();
            List<Integer> fileIdList = new ArrayList<>();

            //파일이 있으면 넣어 주고 파일이 없으면 패스
            if(fileList != null && fileList.size() > 0) {
                for (Object item : fileList) {
                    //file 목록을 처음엔 id(숫자형) 리스트 ( [0,1,2] 형태) 로 받다가 file object list 형태로 바꾸면서
                    //둘 다 오류 없이 수용 가능 하도록 아래와 같이 코딩
                    if (item instanceof LinkedHashMap) {            // List<File Object> 일때
                        LinkedHashMap obj = (LinkedHashMap) item;
                        fileIdList.add((Integer) obj.get("id"));
                    } else if (item instanceof Integer) {           // List<Integer> 일때
                        fileIdList.add((Integer) item);
                    } else if (item instanceof Long) {              // List<Long> 일때
                        fileIdList.add(((Long) item).intValue());
                    }
                }

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("functionId", function.getId());
                paramMap.put("fileList", fileIdList);
                int setResult = chatbotInfoMapper.setFunctionFileMapping(paramMap);

                log.debug("setResult:" + setResult);
            }
        }
        return updateResult;
    }

    @Transactional
    public void insertFunctionInfo(MemberDetail user, FunctionVO function) {

        Map<String,Object> insParamMap = new HashMap<String,Object>();
        insParamMap.put("userKey", user.getUserKey());
        insParamMap.put("functionInfo", function);

        chatbotInfoMapper.insertFunctionInfo(insParamMap);

        Long functionId = (Long) insParamMap.get("id");

        if(functionId > 0) {

            //조직 매핑 테이블 insert
            //chatbotinfo-organization 매핑 테이블 insert
            Long organizationId = memberOrganizationService.getMemberOrganizationId(user.getUserKey());
            chatbotInfoMapper.insertFunctionOrganization(Map.of("functionId", functionId, "organizationId", organizationId));

            //파일처리
            List<Object> fileList = function.getFileList();
            List<Integer> fileIdList = new ArrayList<>();

            if(fileList != null && fileList.size() > 0) {
                for (Object item : fileList) {
                    if (item instanceof LinkedHashMap) {
                        LinkedHashMap obj = (LinkedHashMap) item;
                        fileIdList.add((Integer) obj.get("id"));
                    } else if (item instanceof Integer) {
                        fileIdList.add((Integer) item);
                    } else if (item instanceof Long) {
                        fileIdList.add(((Long) item).intValue());
                    }
                }

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("functionId", functionId);
                paramMap.put("fileList", fileIdList);
                int setResult = chatbotInfoMapper.setFunctionFileMapping(paramMap);

                log.debug("setResult:" + setResult);
            }
        }

    }

    public List<Map<String,Object>> getFunctionInfoListByIds(List<Long> idList) {
        return chatbotInfoMapper.selectFunctionListByIds(idList);
    }

    public Map<String, Object> getOptionList(Long userId) {

        Long organizationId = memberOrganizationService.getMemberOrganizationId(userId);

        List<EngineEntity> llmEngineList = engineService.getEngineList("LLM", null, false, organizationId);
        List<EngineEntity> ragEngineList = engineService.getEngineList("RAG", null, false, organizationId);
        List<FunctionVO> functionList = chatbotInfoMapper.selectFunctionListByUserId(userId);
        List<ElasticVO> elasticList = engineService.getElasticEngineList();

        Map<String,Object> rtnMap = new HashMap<>();
        rtnMap.put("llmEngineList", llmEngineList);
        rtnMap.put("ragEngineList", ragEngineList);
        rtnMap.put("functionList", functionList);
        rtnMap.put("elasticList", elasticList);

        return rtnMap;
    }

    public List<Map<String, Object>> getChatbotInfoMap(Long chatbotId) {
        /*
        // Redis key를 생성
        String redisKey = REDIS_CHATBOTINFO_KEY + ":" + chatbotId;

        // Redis에서 데이터 조회
        List<Map<String, Object>> chatbotInfo = null;

        try {
            chatbotInfo = (List<Map<String, Object>>) redisTemplate.opsForValue().get(redisKey);

            if(chatbotInfo != null)
                for (Map<String, Object> item : chatbotInfo) {
                    Object jsonValue = item.get("json_value");

                    // json_value가 LinkedHashMap으로 되어 있으면 PGobject로 변환
                    if (jsonValue instanceof LinkedHashMap) {
                        LinkedHashMap<String, Object> jsonValueMap = (LinkedHashMap<String, Object>) jsonValue;
                        try {
                            PGobject pgObject = new PGobject();
                            pgObject.setType((String) jsonValueMap.get("type"));
                            pgObject.setValue((String) jsonValueMap.get("value"));
                            item.put("json_value", pgObject); // PGobject로 변환 후 다시 저장
                        } catch (SQLException e) {
                            e.printStackTrace();  // 예외 처리
                        }
                    }
                }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redis에 데이터가 없으면 DB에서 가져옴
        if (chatbotInfo == null) {
            chatbotInfo = chatbotInfoMapper.selectChatbotInfoMapById(chatbotId);

            // Redis에 데이터 저장 (예: 10분간 캐싱)
            if (chatbotInfo != null && !chatbotInfo.isEmpty()) {
                redisTemplate.opsForValue().set(redisKey, chatbotInfo, 10, TimeUnit.MINUTES);
            }
        }

        return chatbotInfo;
        */

        return chatbotInfoMapper.selectChatbotInfoMapById(chatbotId);
    }

    public Map<String, Object> getUserPromptByChatbotId(Long chatbotId) {
        return chatbotInfoMapper.selectUserPromptByChatbotId(chatbotId);
    }

    public Map<String, Object> getLibraryDetailByUserId(Long userId) {
        return chatbotInfoMapper.selectLibraryDetailByUserId(userId);
    }

    public ChatbotInfoIdEntity getGetbotInfoById(Long chatbotId) {
        Optional<ChatbotInfoIdEntity> chatbotInfo = chatbotInfoRepository.findById(chatbotId);
        return chatbotInfo.orElse(null);
    }

    public Map<String,Object> getGetChatbotInfoNormal(Long chatbotId) {
        return chatbotInfoMapper.selectChatbotInfoNormalById(chatbotId);
    }

    public ChatbotInfoIdEntity getGetbotInfoByIdAndUserId(Long chatbotId, String userId) {
        ChatbotInfoIdEntity chatbotInfo = chatbotInfoRepository.findChatbotInfoIdEntityByUserIdAndId(userId, chatbotId);
        return chatbotInfo;
    }


    @Transactional
    public void updateChatbotInfo(Long chatbotId, ChatbotInfoVO chatbotInfo, MemberDetail user) {
        try {

            /*
            // Redis key 생성
            String redisKey = REDIS_CHATBOTINFO_KEY + ":" + chatbotId;

            // Redis에서 해당 키 삭제
            redisTemplate.delete(redisKey);
            */

            Map<String, Object> rootMap = new HashMap<String, Object>();
            rootMap.put("name", chatbotInfo.getName());
            rootMap.put("description", chatbotInfo.getDescription());
            rootMap.put("imgFileId", chatbotInfo.getImgFileId());
            rootMap.put("publicUseYn", chatbotInfo.getPublicUseYn());
            rootMap.put("hiddenYn", chatbotInfo.getHiddenYn());

            String embeddingStatus = "C";

//            try {
//                embeddingStatus = chatbotInfo.getRag().getUseYn() == true?"P":"C";
//            } catch (Exception e) {
//
//            }

            rootMap.put("embeddingStatus", embeddingStatus);

            // Update memory type and window size
            chatbotInfoMapper.updateChatbotInfoMemoryTypeAndWindowSize(Map.of(
                    "chatbotId", chatbotId,
                    "llmCommon", chatbotInfo.getLlmCommon(),
                    "root", rootMap
            ));
            log.info("Updated memory type and window size for chatbotId: {}", chatbotId);

            // Update normal conversation LLM
            chatbotInfoMapper.updateNormalConversationLLM(Map.of(
                    "chatbotId", chatbotId,
                    "normalConversation", chatbotInfo.getNormalConversation()
            ));
            log.info("Updated normal conversation LLM for chatbotId: {}", chatbotId);

            // Update reproduce question LLM
            chatbotInfoMapper.updateReproduceQuestionLLM(Map.of(
                    "chatbotId", chatbotId,
                    "reproduceQuestion", chatbotInfo.getReproduceQuestion()
            ));
            log.info("Updated reproduce question LLM for chatbotId: {}", chatbotId);

            // Update RAG LLM
            ChatbotInfoVO.Rag ragInfo = chatbotInfo.getRag();
            if(ragInfo != null) {
                chatbotInfoMapper.updateRagLLM(Map.of(
                        "chatbotId", chatbotId,
                        "rag", ragInfo
                ));
                log.info("Updated RAG LLM for chatbotId: {}", chatbotId);

                if(ragInfo.getFunctionRetry() == null)
                    ragInfo.setFunctionRetry(2);
                if(ragInfo.getFunctionLlmEngineId() == null)
                    ragInfo.setFunctionLlmEngineId(2);
                if(ragInfo.getFunctionFallbackEngineId() == null)
                    ragInfo.setFunctionFallbackEngineId(3);

                int rtnFnCall = chatbotInfoMapper.updateFunctionCallLLM(Map.of(
                        "chatbotId", chatbotId,
                        "fnLlm", ragInfo
                ));

                if(rtnFnCall < 1) {
                    rtnFnCall = chatbotInfoMapper.insertFunctionCallLLM(Map.of(
                            "chatbotId", chatbotId,
                            "fnLlm", ragInfo
                    ));
                    log.info("Updated(Inserted) Function LLM for chatbotId: {}, {}", chatbotId, rtnFnCall);
                } else
                    log.info("Updated Function LLM for chatbotId: {}, {}", chatbotId, rtnFnCall);

                // Update Elastic in Rag
                ChatbotInfoVO.ElasticSearch elasticSearch = ragInfo.getElasticSearch();
                            chatbotInfoMapper.updateChatbotInfoElasticSearch(Map.of(
                                    "chatbotId", chatbotId,
                                    "elasticSearch", elasticSearch
                            ));

            }
            chatbotInfo.setId(chatbotId);
            //라그 켜졌을때만 실행하자.
            if(ragInfo != null && ragInfo.getUseYn())
                embeddingFunctions(chatbotInfo, user);
            log.info("start channel reset...");
            chatGrpcConnectionHandler.resetChannelByChatbotId(chatbotId);
            log.info("end channel reset...");
        } catch (Exception e) {
            log.error("챗봇 정보 업데이트 실패: {}", e.getMessage(), e);
            throw new RuntimeException("챗봇 정보 업데이트 실패: " + e.getMessage(), e);
        }
    }

    public void embeddingFunctions(ChatbotInfoVO chatbotInfo, MemberDetail user) {
        Boolean bEmbeddingPass = true;
        log.info("Start embeddingFunctions..." + DateUtil.convertToStringByMs(System.currentTimeMillis()));
        ChatbotInfoVO.Rag ragInfo = chatbotInfo.getRag();

        if (ragInfo == null || ragInfo.getFunctions() == null || ragInfo.getFunctions().size() < 1) {
            log.error("embeddingFunctions Error!!! - RagInfo/Functions is null or empty");
            return;
        }

        //평선 단위 신규 엠베딩 여부 체크.
        List<Long> functions = ragInfo.getFunctions();
        List<Long> embeddingTarget = new ArrayList<Long>();

        //펑션이 없으면 엠베딩 하지 않음.
        if(functions == null ||functions.size() < 1) {
            log.debug("function is null or empty. skip embedding...");
            return;
        }

        for(Long functionId:functions) {
            List<Long> newMappingList = chatbotInfoMapper.selectFunctFileMappingList(functionId);
            List<Long> nowMappingList = chatbotInfoMapper.selectChatbotFunctionFileMappingList(chatbotInfo.getId(), functionId, Long.valueOf(ragInfo.getEmbeddingEngineId()));

            String newMappingListString = (newMappingList==null?"":newMappingList.stream().map(String::valueOf).collect(Collectors.joining(","))) ;
            String nowMappingListString = (nowMappingList==null?"":nowMappingList.stream().map(String::valueOf).collect(Collectors.joining(","))) ;

            log.debug("compare:" + newMappingListString + ":" + nowMappingListString);

            if(newMappingList != null && !newMappingList.equals(nowMappingList)) {
                //엠베딩 대상에 추가
                log.debug("add to embedding target!!!:" + functionId);
                embeddingTarget.add(functionId);
                chatbotInfoMapper.deleteEmbeddingStatusByFunctionId(chatbotInfo.getId(), functionId); //기존 매핑 정보 삭제
            } else {
                log.debug("pass add to embedding target!!!:" + functionId);
            }
        }

        //mapping status 테이블에서 삭제 대상 function 삭제 처리
        chatbotInfoMapper.deleteEmbeddingStatusByFunctionIdNotInclude(chatbotInfo.getId(), functions);

        for(Long functionId:embeddingTarget) {
            String directoryName = chatbotInfo.getId() + "_" + functionId;
            String destinationDirectory = S3_PATH + "/" + directoryName;
            try {
                //폴더 생성
                Path destinationPath = Paths.get(destinationDirectory);

                // 대상 디렉토리가 존재하지 않으면 생성
                if (Files.exists(destinationPath)) { //존재하면 삭제 후 생성.
                    Path parentPath = destinationPath.getParent();
                    Path bakPath = IntStream.iterate(0, i -> i + 1)
                            .mapToObj(i -> parentPath.resolve("_bak_" + directoryName + "_" + i))
                            .filter(p -> !Files.exists(p))
                            .findFirst()
                            .orElseThrow(() -> new IOException("Failed to find a unique directory name."));
                    Files.move(destinationPath, bakPath);
                }
                Files.createDirectories(destinationPath);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            //해당 폴더에 파일 copy
            List<SourceFileEntity> fileList = chatbotInfoMapper.selectFileListByFunctionId(functionId);
            List<ChatbotInfoEmbeddingStats> statusMappingList = new ArrayList<ChatbotInfoEmbeddingStats>();

            for(SourceFileEntity fileObj:fileList) {
                log.debug(fileObj.getOrgName() + ":" + fileObj.getName());
                try {
                    Path sourcePath = Paths.get(fileObj.getPath());
                    Path destinationPath = Paths.get(destinationDirectory);
                    Path targetPath = destinationPath.resolve(sourcePath.getFileName());
//                    Path targetPath = destinationPath.resolve(fileObj.getOrgName());
                    Files.copy(sourcePath, targetPath);
                    ChatbotInfoEmbeddingStats statusItem = new ChatbotInfoEmbeddingStats(
                            chatbotInfo.getId(), functionId,  Long.valueOf(fileObj.getId()), Long.valueOf(ragInfo.getEmbeddingEngineId()), "T");
                    statusMappingList.add(statusItem);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }

            if(statusMappingList != null && statusMappingList.size() > 0) {
                bEmbeddingPass = false;
                chatbotInfoMapper.insertChatbotEmbeddingStatus(statusMappingList);
                //파일 copy가 끝나면 embedding status에 값 insert 후 api 호출
                for(Long fid:embeddingTarget) {
                    int rst = chatbotInfoMapper.updateChatbotInfoEmbeddingStatus(chatbotInfo.getId(), "P");
                    log.debug("updateChatbotInfoEmbeddingStatus:" + chatbotInfo.getId() + ":" + fid + ":" + rst);
                }
                for(Long fid:embeddingTarget) {

                    //Long embeddingEngineId = chatbotInfo.getRag().getEmbeddingEngineId();
//                    chatbotInfo.getRag().getElasticSearch()


                    // data = request.json
                    // chatbot_id = data.get('chatbot_id')
                    // folder_name = data.get('folder_name')
                    // model = data.get('model')
                    // es_index = data.get('index')
                    // es_url = data.get('url')

                    int embeddingEngineId = chatbotInfo.getRag().getEmbeddingEngineId();
                    int elasticId = chatbotInfo.getRag().getElasticSearch().getEndpoint();

                    EngineVO engineInfo = engineService.getEngineByIdWithMapper(Long.valueOf(embeddingEngineId));
                    ElasticVO elasticInfo = engineService.getElasticEngineByIdWithMapper(Long.valueOf(elasticId));

                    String modelVendor = engineInfo.getVendor();
                    String modelName = engineInfo.getModel();
                    String esIndex = elasticInfo.getIndex1();
                    String esUrl = elasticInfo.getUrl();

                    embeddingService.callEmbedding(chatbotInfo.getId(), fid, modelVendor, modelName, esIndex, esUrl, statusMappingList);

//                    if(!rst) {
//                        chatbotInfoMapper.updateChatbotInfoEmbeddingStatus(chatbotInfo.getId(), "E");
//                        Map<String,Object> param = new HashMap<String,Object>();
//                        param.put("embeddingStatus", "E");
//                        param.put("list", statusMappingList);
//                        chatbotInfoMapper.updateChatbotInfoElasticSearch(param);
//                    }

                    log.debug("callEmbedding: [{}], [{}], [{}], [{}], [{}], [{}]",
                            chatbotInfo.getId(), fid ,modelVendor, modelName, esIndex, esUrl);
                }
            }
        }

        if(bEmbeddingPass) {
            log.debug("check embedding pass ok!!!");
            chatbotInfoMapper.updateChatbotInfoEmbeddingStatus(chatbotInfo.getId(), "C");
        }

    }


    @Transactional
    public void insertChatbotInfo(ChatbotInfoVO chatbotInfo, MemberDetail user) {
        try {
            Map<String, Object> rootMap = new HashMap<String, Object>();
            rootMap.put("name", chatbotInfo.getName());
            rootMap.put("description", chatbotInfo.getDescription());
            rootMap.put("imgFileId", chatbotInfo.getImgFileId());
            rootMap.put("publicUseYn", chatbotInfo.getPublicUseYn());
            rootMap.put("hiddenYn", chatbotInfo.getHiddenYn());

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("userId", user.getUserKey());
            params.put("llmCommon", chatbotInfo.getLlmCommon());
            params.put("root", rootMap);

            //chatbot_info 테이블
            chatbotInfoMapper.insertChatbotInfoMemoryTypeAndWindowSize(params);
            Long chatbotId = (Long) params.get("id");
            log.info("Inserted memory type and window size for chatbotId: {}", chatbotId);

            // Insert normal conversation LLM
            // chatbot_detail_llm 테이블 : SELECT cd.cd_id FROM code cd WHERE cd.enum = 'normal_conversation' AND cd.cdgroup_id = 'CHATBOT_LLM_TYPE'
            chatbotInfoMapper.insertNormalConversationLLM(Map.of(
                    "chatbotId", chatbotId,
                    "normalConversation", chatbotInfo.getNormalConversation(),
                    "llmCommon", chatbotInfo.getLlmCommon()
            ));
            log.info("Inserted normal conversation LLM for chatbotId: {}", chatbotId);

            // Insert reproduce question LLM
            // chatbot_detail_llm 테이블 : SELECT cd.cd_id FROM code cd WHERE cd.enum = 'reproduce_question' AND cd.cdgroup_id = 'CHATBOT_LLM_TYPE'
            chatbotInfoMapper.insertReproduceQuestionLLM(Map.of(
                    "chatbotId", chatbotId,
                    "reproduceQuestion", chatbotInfo.getReproduceQuestion(),
                    "llmCommon", chatbotInfo.getLlmCommon()
            ));
            log.info("Inserted reproduce question LLM for chatbotId: {}", chatbotId);

            ChatbotInfoVO.Rag ragInfo = chatbotInfo.getRag();

            String param0 = ragInfo.getFunctionsString();

            List<ChatbotInfoVO.EmbeddingType> embeddingTypeList = ragInfo.getEmbeddingType();
            String param1 = "N";
            String param2 = "N";

            for(ChatbotInfoVO.EmbeddingType type : embeddingTypeList) {
                switch(type.getId()) {
                    case("bm25"):
                        param1 = type.getValue()==1?"Y":"N";
                        break;
                    case("openai"):
                        param2 = type.getValue()==1?"Y":"N";
                        break;
                }
            }

            // Insert RAG LLM
            // chatbot_detail_llm 테이블 : SELECT cd.cd_id FROM code cd WHERE cd.enum = 'rag' AND cd.cdgroup_id = 'CHATBOT_LLM_TYPE')
            chatbotInfoMapper.insertRagLLM(Map.of(
                    "chatbotId", chatbotId,
                    "rag", ragInfo,
                    "llmCommon", chatbotInfo.getLlmCommon(),
                    "param0", param0,
                    "param1", param1,
                    "param2", param2
            ));
            log.info("Inserted RAG LLM for chatbotId: {}", chatbotId);

            if(ragInfo.getFunctionRetry() == null)
                ragInfo.setFunctionRetry(2);
            if(ragInfo.getFunctionLlmEngineId() == null)
                ragInfo.setFunctionLlmEngineId(2);
            if(ragInfo.getFunctionFallbackEngineId() == null)
                ragInfo.setFunctionFallbackEngineId(3);

            int rtnInsFnLLm = chatbotInfoMapper.insertFunctionCallLLM(Map.of(
                    "chatbotId", chatbotId,
                    "fnLlm", ragInfo
            ));
            log.info("Inserted Function LLM for chatbotId: {}, {}", chatbotId, rtnInsFnLLm);

            // Insert Elastic Search details
            ChatbotInfoVO.ElasticSearch elasticSearch = ragInfo.getElasticSearch();
            // chatbot_detail_elastic 테이블
            chatbotInfoMapper.insertChatbotInfoElasticSearch(Map.of(
                    "chatbotId", chatbotId,
                    "elasticSearch", elasticSearch
            ));
            log.info("Inserted Elastic Search details for chatbotId: {}", chatbotId);

            //chatbotinfo-organization 매핑 테이블 insert
            Long organizationId = memberOrganizationService.getMemberOrganizationId(user.getUserKey());
            chatbotInfoMapper.insertChatbotOrganization(Map.of("chatbotId", chatbotId, "organizationId", organizationId));

            chatbotInfo.setId(chatbotId);
            if(ragInfo != null && ragInfo.getUseYn())
                embeddingFunctions(chatbotInfo, user);
        } catch (Exception e) {
            log.error("챗봇 정보 삽입 실패: {}", e.getMessage(), e);
            throw new RuntimeException("챗봇 정보 삽입 실패: " + e.getMessage(), e);
        }
    }

    public List<Map<String,Object>> selectEmbeddingStatusList(List<Long> idList) {
        List<String> resultListString = chatbotInfoMapper.selectEmbeddingStatusList(idList);

        List<Map<String, Object>> resultList = resultListString.stream()
                .map(jsonString -> new JSONObject(jsonString).toMap())  // JSONObject를 Map으로 변환
                .collect(Collectors.toList());

        return resultList;
    }

    public Map<String,Object> selectEmbeddingStatus(Long chatbotId) {
        List<Long> idList = List.of(chatbotId);
        List<Map<String,Object>> resultList = selectEmbeddingStatusList(idList);

        if(resultList == null || resultList.size() != 1)
            return null;
        else
            return resultList.get(0);
    }

    public int deleteChatbotInfo(Long id) {
        return chatbotInfoMapper.deleteChatbotInfo(id);
    }

    public int deleteFunction(Long id) {
        return chatbotInfoMapper.deleteFunction(id);
    }

    public ChatbotContent getChatBotContentWithinChatbotInfo(Long chatbotId) {
        ChatbotContent chatbotContent = new ChatbotContent();
        List<ChatbotContentCard> chatbotContentCardList = new ArrayList<ChatbotContentCard>();

        try {

            Map<String, Object> contentsMap = chatbotInfoMapper.selectContentsInfoInChatbotInfo(chatbotId);

            if (contentsMap != null) {
                log.info("contentsMap:" + contentsMap.get("name") + ":" + contentsMap.get("functions"));
                ChatbotContentTitle title = new ChatbotContentTitle(String.valueOf(contentsMap.get("name")));
                ChatbotContentComment comment = new ChatbotContentComment("ico_chatbot.svg", String.valueOf(contentsMap.get("description")));

                PGobject tmpObj = (PGobject) contentsMap.get("functions");
                JSONArray jsonArray = new JSONArray(tmpObj.getValue());

                for(int i = 0 ; i < jsonArray.length() ; i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    String questionName = item.getString("question_name");
                    String questionDetail = item.getString("question_detail");
                    String questionImage = item.getString("question_image");

                    if(questionName == null || questionName.isBlank())
                        questionName = item.getString("name");

                    if(questionDetail == null || questionDetail.isBlank())
                        questionDetail = item.getString("description");

                    if(questionImage == null || questionImage.isBlank())
                        questionImage = "0";

//                    questionImage = "http://localhost:9993/file/image/" + questionImage;

                    ChatbotContentCard card = new ChatbotContentCard(questionImage, questionName, questionDetail);
                    chatbotContentCardList.add(card);

                    log.debug(questionName + ":" + questionDetail + ":" + questionImage);
                }

                log.info("tmp...");
                chatbotContent.setTitle(title);
                chatbotContent.setComment(comment);
                chatbotContent.setCards(chatbotContentCardList);
            }
        } catch (Exception e) {
            log.error("chatbotContents Error!!!" + chatbotId);
        }

        /*

        List<ChatbotContentEntity> chatbotContentList = chatbotContentRepository.findByChatbotIdOrderByTypeCdAscSeqAsc(chatbotId);

        if(chatbotContentList == null || chatbotContentList.size() < 1) {
            //해당 챗봇에 contents가 없으면 기본 컨텐츠 (chatbotId = 0) 인 contents를 리턴
            chatbotContentList = chatbotContentRepository.findByChatbotIdOrderByTypeCdAscSeqAsc(0L);
        }

        LogUtil.debug("chatbotContentList:" + chatbotContentList.size());

        for(ChatbotContentEntity item:chatbotContentList) {
            switch(item.getTypeCd()) {
                case("TITLE"):
                    chatbotContent.setTitle(
                            new ChatbotContentTitle(item.getText())
                    );
                    break;
                case("COMMENT"):
                    chatbotContent.setComment(
                            new ChatbotContentComment(
                                    item.getImg(),
                                    item.getText()
                            )
                    );
                    break;
                case("CARD"):
                    item.getImg();
                    item.getTitle();
                    item.getText();

                    chatbotContentCardList.add(
                            new ChatbotContentCard(
                                    item.getImg(),
                                    item.getTitle(),
                                    item.getText()
                            )
                    );
                    break;
                default:
                    break;
            }

            chatbotContent.setCards(chatbotContentCardList);

        }

        if(chatbotContent.getTitle() == null
                && chatbotContent.getComment() == null
                && chatbotContent.getCards() == null) {
            throw BaseException.of("데이터가 없습니다.");
//            chatbotContent = null;
        }
        */
        return chatbotContent;
    }

    public boolean checkFunctionUsage(Long functionId) {
        Boolean result = false;
        List<Map<String,Object>> chatbotInfoList = chatbotInfoMapper.getChatbotInfoByFunctionId(functionId);
        if(chatbotInfoList != null && !chatbotInfoList.isEmpty()) {
            log.debug("Check Function Usage - 사용중:삭제불가 : {}, {}", functionId, chatbotInfoList.size());
            result = true;
        } else {
            log.debug("Check Function Usage - 사용하지 않음:삭제가능 : {}", functionId);
        }
        return result;
    }
}
