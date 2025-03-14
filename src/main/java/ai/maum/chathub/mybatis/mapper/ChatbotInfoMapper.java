package ai.maum.chathub.mybatis.mapper;

import ai.maum.chathub.api.file.entity.SourceFileEntity;
import ai.maum.chathub.mybatis.vo.ChatbotInfoEmbeddingStats;
import ai.maum.chathub.mybatis.vo.FunctionVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface ChatbotInfoMapper {
    //    get
    public List<Map<String, Object>> selectChatbotInfoMapById(Long chatbotId);
    public List<Map<String, Object>> selectChatbotInfoListByUserId(String userId);
    public List<Map<String, Object>> selectChatbotInfoList(Map<String,Object> param);
    public List<Map<String, Object>> selectChatbotInfoListForSuperAdmin(Map<String,Object> param);
    public List<Map<String, Object>> selectChatbotInfoListForAdmin(Map<String,Object> param);
    public List<Map<String, Object>> selectChatbotInfoListForEditor(Map<String,Object> param);
    public List<Map<String, Object>> selectChatbotInfoListForUser(Map<String,Object> param);
    public List<FunctionVO> selectFunctionListByChatbotId(Long chatbotId);
    public List<Map<String,Object>> selectFunctionListByIds(List<Long> idList);
    public List<FunctionVO> selectFunctionListByUserId(Long userId);
//    public List<FunctionVO> selectFunctionList(Long userKey);
    public List<FunctionVO> selectFunctionList(Map<String, Object> param);
    public FunctionVO selectFunctionInfoById(Long id);

    public Map<String, Object> selectUserPromptByChatbotId(Long chatbotId); //임시
    public Map<String, Object> selectLibraryDetailByUserId(Long userId);
    public Map<String, Object> selectContentsInfoInChatbotInfo(Long chatbotId);
    public Map<String, Object> selectChatbotInfoNormalById(Long chatbotId);

    public List<Map<String, Object>> selectFunctionFileMappingListByFunctionId(List<Long> idList);
    List<Long> selectFunctFileMappingList(Long functionId);
    List<Long> selectChatbotFunctionFileMappingList(Long chatbotId, Long functionId, Long embeddingEngineId);
    public List<ChatbotInfoEmbeddingStats> selectChatbotFunctionFileMappingListByChatbotId(Long chatbotId);

    List<SourceFileEntity> selectFileListByFunctionId(Long functionId);
    List<String> selectEmbeddingStatusList(List<Long> idList);

    List<Map<String,Object>> getChatbotInfoByFunctionId(@Param("functionId") Long functionId);

    int updateChatbotInfoEmbeddingStatus(Long id, String embeddingStatus);
    int deleteEmbeddingStatusByFunctionId(Long chatbotId, Long functionId);
    int deleteEmbeddingStatusByFunctionIdNotInclude(Long chatbotId, List<Long> idList);
    int insertChatbotEmbeddingStatus(List<ChatbotInfoEmbeddingStats> chatbotInfoEmbeddingStats);
    int updateChatbotEmbeddingStatus(Map<String,Object> param);

    int updateFunctionInfo(FunctionVO function);
    int insertFunctionInfo(Map<String,Object> params);

    //    update
    int updateChatbotInfoMemoryTypeAndWindowSize(Map<String, Object> params);

    int updateChatbotInfoUserPrompts(Map<String, Object> params);

    int updateNormalConversationLLM(Map<String, Object> params);

    int updateReproduceQuestionLLM(Map<String, Object> params);

    int updateRagLLM(Map<String, Object> params);

    int updateFunctionCallLLM(Map<String, Object> params);

    int updateChatbotInfoElasticSearch(Map<String, Object> params);

    int updateChatbotInfoFunctionCalls(Map<String, Object> params);

    //    insert
    int insertChatbotInfoMemoryTypeAndWindowSize(Map<String, Object> params);

    int insertChatbotInfoUserPrompts(Map<String, Object> params);

    int insertNormalConversationLLM(Map<String, Object> params);

    int insertReproduceQuestionLLM(Map<String, Object> params);

    int insertRagLLM(Map<String, Object> params);

    int insertFunctionCallLLM(Map<String, Object> params);

    int insertChatbotInfoElasticSearch(Map<String, Object> params);

    int insertChatbotInfoFunctionCalls(Map<String, Object> params);

    int resetFunctionFileMapping(Long id);
    int setFunctionFileMapping(Map<String,Object> params);

    int deleteChatbotInfo(Long id);
    int deleteFunction(Long id);

    int insertChatbotOrganization(Map<String, Object> params);
    int insertFunctionOrganization(Map<String, Object> params);
}