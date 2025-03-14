package ai.maum.chathub.api.admin;

import ai.maum.chathub.api.admin.entity.ChatMonitorLogEntity;
import ai.maum.chathub.api.admin.service.MonitorLogService;
import ai.maum.chathub.api.chat.handler.ChatGrpcConnectionHandler;
import ai.maum.chathub.api.chatbot.service.ChatbotService;
import ai.maum.chathub.api.chatroom.entity.ChatroomEntity;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.routine.entity.RoutineDetailEntity;
import ai.maum.chathub.api.skins.service.CallSkinsService;
import ai.maum.chathub.api.weather.service.WeatherService;
import ai.maum.chathub.api.kakao.dto.FriendTalkResponse;
import ai.maum.chathub.api.kakao.service.KakaoService;
import ai.maum.chathub.conf.security.SecurityConfig;
import ai.maum.chathub.mybatis.vo.MonitorLogVO;
import ai.maum.chathub.scheduler.ResourceCheckScheduler;
import ai.maum.chathub.util.CryptoUtil;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.util.ObjectMapperUtil;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.zaxxer.hikari.HikariDataSource;
import rag_service.rag_module.Rag;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/maum-admin/api")
@RequiredArgsConstructor
@Tag(name="Admin-운영", description="서비스 오픈전 테스트 및 운영을 위한 임시 Rest Controller")
public class AdminManagementRestController {

    private final MonitorLogService monitorLogService;
    private final ChatroomService chatroomService;
    private final ChatbotService chatbotService;
    private final HikariDataSource datasource;
    private final CallSkinsService callSkinsService;
    private final WeatherService weatherService;
    private final KakaoService kakaoService;
    private final ResourceCheckScheduler resourceCheckScheduler;
    private final PasswordEncoder passwordEncoder;
    private final SecurityConfig securityConfig;

    private final ChatGrpcConnectionHandler connectionHandler;
    @Operation(summary = "grpc connectioin reset", description = "grpc connectioin reset")
    @GetMapping({"/grpc/reset/{chatroom_id}", "/grpc/reset"})
    @ResponseBody
    public String grpcReset(
            @PathVariable(name="chatroom_id", required=false) @Parameter(name = "챗룸ID", required = false) String chatroomId
    ) {
        try {
            int beforeSize = connectionHandler.getConnectionSize();

            int afterSize = 0;
            if(chatroomId == null || chatroomId.isBlank())
                afterSize = connectionHandler.connectionReset();
            else {
                boolean isSuccess = connectionHandler.connectionReset(Long.valueOf(chatroomId));
                afterSize = connectionHandler.getConnectionSize();
            }

            String msg = "Total Connection size : " + beforeSize + ", after Connection size: " + afterSize;

            LogUtil.info("grpcReset: " + msg);

            return msg;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Operation(summary = "grpc connectioin list", description = "grpc connectioin list")
    @GetMapping({"/grpc/list"})
    @ResponseBody
    public Map<String,Object> getGrpcConnectionsList() {
        return connectionHandler.getConnectoins();
    }


    @PostMapping("/llmask")
    public @ResponseBody Object llmAsk(
            @RequestBody Object question
    ) {

        log.debug("question:" + question);

        return question;
    }

    @GetMapping("/loglist")
    @ResponseBody
    public List<MonitorLogVO>  getMonitorLogList(
            @RequestParam (value="log_date") String logDate
    ) {
        return monitorLogService.getRecentLogList(logDate);
    }

    @GetMapping("/logs")
    @ResponseBody
    public Map<String, Object> getMonitorLog(
            @RequestParam (value="room_id") String param1, @RequestParam (value="seq") String param2
            ) {

        Map<String,Object> rtnObject = new HashMap<String,Object>();

        Long roomId = Long.valueOf(param1);
        Long seq = Long.valueOf(param2);

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
            logs2.add(newItem);
        }
        rtnObject.put("logs", logs2);
        rtnObject.put("total_time", totalTime);

        //roomId 기반으로 chatbot의 prompt 가져옴.
        ChatroomEntity chatroomEntity = chatroomService.getChatroom(roomId);
//        ChatbotEntity chatbot = chatbotService.getChatbotById(chatroomEntity.getChatbotId());

        rtnObject.put("chatbot_id", chatroomEntity.getChatbotId());
//        rtnObject.put("chatbot_id", chatbot.getId());
//        rtnObject.put("prompt_1", chatbot.getPromptRequirement());
//        rtnObject.put("prompt_2", chatbot.getPromptTail());

        return rtnObject;
    }

    @PostMapping("/save-prompts")
    @ResponseBody
    public Object savePrompts(@RequestBody Map<String, Object> promptData) {

        String prompt1 = (String) promptData.get("prompt1");
        String prompt2 = (String) promptData.get("prompt2");
        String chatbotId = (String) promptData.get("chatbotId");

        LogUtil.debug("prompt1:" + prompt1);
        LogUtil.debug("prompt2:" + prompt2);
        LogUtil.debug("chatbotId:" + chatbotId);

        monitorLogService.savePrompts(prompt1, prompt2, chatbotId);

        Map<String,Object> result = new HashMap<String,Object>();
        result.put("result", true);

        return result;
    }

    @GetMapping("/dbinfo")
    @ResponseBody
    public String getDBInfo() {
        HikariPoolMXBean hikariBean = datasource.getHikariPoolMXBean();
        String poolCntString = "Total:" + hikariBean.getTotalConnections() + "\n" +
                               "Active:" + hikariBean.getActiveConnections() + "\n" +
                               "Idle:" + hikariBean.getIdleConnections();
        log.debug(poolCntString);
        return poolCntString;
    }

    @GetMapping("/skins/test")
    @ResponseBody
    public String callSkins(
            @RequestParam (value="uri") String uri,
            @RequestParam (value="type", required = false) String type
    ) {
        HttpMethod httpMethod = HttpMethod.GET;
        if("POST".equals(type))
            httpMethod = HttpMethod.POST;

        return callSkinsService.callSkins(uri, httpMethod);
    }

    @GetMapping("/weather/test")
    @ResponseBody
    public String callWeather(
            @RequestParam(value="lo", required = false) Float longitude,
            @RequestParam(value="la", required = false) Float latitude,
            @RequestParam(value="d", required = false) String targetDate,
            @RequestParam(value="t", required = false) String targetTime
    ) {
        return weatherService.callWeather(longitude, latitude, targetDate, targetTime);
    }


    /*
    @GetMapping("/check/schedule")
    @ResponseBody
    public Map<String,String> checkScheduler(
//            @RequestParam (value="uri") String uri
    ) {
        ConcurrentHashMap<String, ChatGrpcConnectionHandler.ChatRequestObject> chatRequestMap = connectionHandler.getConnectionMaps();

        Map<String,String> resultMap = new HashMap<String,String>();

        chatRequestMap.forEach( (key, value) -> {
//            ChatStreamObserverHandler chatHander = value.getStreamObserver();



            ScheduledExecutorService scheduledService = chatHander.getScheduler();
            String rtnString = value.getUserKey() + ":" + value.getChatbotId() + ":" + value.getChatroomId() + ":" + scheduledService.isShutdown() + ":" + scheduledService.isTerminated();
            log.info("schdule:" + key + ":" + rtnString);
            resultMap.put(key, rtnString);
        });

        return resultMap;
    }
    */

    /*
    @GetMapping("/check/schedule/shutdown")
    @ResponseBody
    public Map<String,String> checkScheduler(
            @RequestParam (value="target") String target
    ) {
        ConcurrentHashMap<String, ChatGrpcConnectionHandler.ChatRequestObject> chatRequestMap = connectionHandler.getConnectionMaps();

        Map<String,String> resultMap = new HashMap<String,String>();

        chatRequestMap.forEach( (key, value) -> {
            ChatStreamObserverHandler chatHander = value.getStreamObserver();
            ScheduledExecutorService scheduledService = chatHander.getScheduler();
            String rtnString = "";
            if(target != null && target.equals(key)) {
                log.info("schdule:" + key + " shutdown!!!:" + scheduledService.isShutdown() + ":" + scheduledService.isTerminated());
                if(!scheduledService.isShutdown() || !scheduledService.isTerminated()) {
                    if(!scheduledService.isShutdown()) {
                        scheduledService.shutdownNow();
                    }
                    rtnString = value.getUserKey() + ":" + value.getChatbotId() + ":" + value.getChatroomId() + ":" + scheduledService.isShutdown() + ":" + scheduledService.isTerminated();//                    scheduledService.close();
                    log.info("schdule:" + key + ":" + rtnString);
                    scheduledService = null;
                    log.info("schdule:" + key + " set null forced!!!");
                } else {
                    rtnString = value.getUserKey() + ":" + value.getChatbotId() + ":" + value.getChatroomId() + ":" + scheduledService.isShutdown() + ":" + scheduledService.isTerminated();
                    log.info("schdule:" + key + ":" + rtnString);
                }
            }
            resultMap.put(key, rtnString);
        });

        return resultMap;
    }
    */


    @Operation(summary = "톡(친구톡) 발송", description = "테스트용")
    @ResponseBody
    @PostMapping({"/sendtalk/{sender_id}"})
    public BaseResponse sendFriendTalk (
            @PathVariable(name = "sender_id", required = false) @Parameter(description = "폰번호", required = true) String senderId,
            @RequestParam(name="text", required=false) @Parameter(name = "text", required = true) String text,
            @RequestBody @Parameter(name = "채팅메시지", required = true) List< RoutineDetailEntity > routineDetails
    ) {

        try {

            String resString = kakaoService.sendFriendTalkTest(senderId, text, routineDetails);
            FriendTalkResponse friendTalkResponse = ObjectMapperUtil.readValue(resString, FriendTalkResponse.class);
            String rtnMessage = friendTalkResponse.getMessage();
            if("성공".equals(rtnMessage))
                return BaseResponse.success(rtnMessage);
            else
                return BaseResponse.failure(rtnMessage);
        } catch (Exception e) {
            return BaseResponse.failure(e.getMessage());
        }
    }

    @Operation(summary = "채팅/Proto-Test-Echo", description = "채팅/Proto-Test")
    @PostMapping({"/grpc/echo"})
    public String protoTest(
            @RequestParam(value = "host", defaultValue = "i-dev-mcl-rag-search.apddev.com") String host,
            @RequestParam(value = "port", defaultValue = "443") Integer port,
            @RequestParam(value = "message", defaultValue = "hello grpc!!!") String message
    ) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Insecure channel for simplicity (use SSL/TLS in production)
                .build();

        rag_service.rag_module.RagServiceGrpc.RagServiceBlockingStub stub = rag_service.rag_module.RagServiceGrpc.newBlockingStub(channel);
        Rag.Message requestMessage = Rag.Message.newBuilder()
                .setMsg(message)
                .build();

        Rag.Message responseMessage = stub.echo(requestMessage);

        LogUtil.debug("Response from grpc server: " + responseMessage.getMsg());

        return responseMessage.getMsg();
    }

    @Operation(summary = "채팅/Proto-Test-Echo", description = "채팅/Proto-Test")
    @PostMapping({"/grpc/elastic/index"})
    public String protoTest(
            @RequestParam(value = "host", defaultValue = "i-dev-mcl-rag-search.apddev.com") String host,
            @RequestParam(value = "port", defaultValue = "443") Integer port,
            @RequestParam(value = "index", defaultValue = "1") Integer index
            ) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Insecure channel for simplicity (use SSL/TLS in production)
                .build();

        rag_service.rag_module.RagServiceGrpc.RagServiceBlockingStub stub = rag_service.rag_module.RagServiceGrpc.newBlockingStub(channel);

        Rag.ElasticIndex elasticIndex =
            Rag.ElasticIndex.newBuilder().setEndpointIdxValue(index).build();

        Rag.ElasticResponse response = stub.elasticEndpoint(elasticIndex);

        LogUtil.debug("Response from grpc server: " + response.getResultValue());

        return "result:" + response.getResultValue();
    }

    @GetMapping("/resource/check")
    public String processResourceCheck() {
        resourceCheckScheduler.testResourceChecker();
        return "ResourceCheck!!!";
    }

    @GetMapping("/generate/pwd")
    public String generateUser(
            @RequestParam(value="pwd") String pwd
    ) {
        String hashPwd = CryptoUtil.encode(pwd);
        String encPassword = passwordEncoder.encode(hashPwd);

        log.debug("pwd: {}", pwd);
        log.debug("hashPwd: {}", hashPwd);
        log.debug("encPassword: {}", encPassword);

        return "generateUser";
    }

    @GetMapping("/cors/reload")
    public String reloadCors(
    ) {
        securityConfig.updateCorsConfigurations();
        return "reloadCors!!!";
    }
}
