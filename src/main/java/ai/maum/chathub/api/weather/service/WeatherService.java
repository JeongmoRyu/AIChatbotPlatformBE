package ai.maum.chathub.api.weather.service;

import ai.maum.chathub.api.weather.model.WeatherInfo;
import ai.maum.chathub.util.DateUtil;
import ai.maum.chathub.api.weather.model.Location;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rag_service.rag_module.Rag;
import rag_service.rag_module.RagServiceGrpc;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
//@RequiredArgsConstructor
public class WeatherService {

    @Value("${service.grpc.chat.host}")
    private String host;

    @Value("${service.grpc.chat.port}")
    private int port;

    private Map<Location, WeatherInfo> weatherCache;

    /*
    private ManagedChannel managedChannel;
    private RagServiceGrpc.RagServiceStub stub;

    public WeatherService() {
        this.managedChannel = createChannel();
        this.stub = createStub(this.managedChannel);
    }

    private ManagedChannel createChannel() {
        log.debug("gRPC:createChannel 4 Weather!!!" + host + ":" + port);
        ManagedChannelBuilder channelBuilder = ManagedChannelBuilder.forAddress(host, port);
        if(port != 443)
            channelBuilder.usePlaintext();
        return channelBuilder.build();
    }

    private RagServiceGrpc.RagServiceStub createStub(ManagedChannel channel) {
        return RagServiceGrpc.newStub(channel);
    }
    */
    public String callWeather(Float longitude, Float latitude, String targetDate, String targetTime) {

        String rtnString = "";

        try {

            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext() // 보안 없이 통신
                    .build();

            // gRPC 서비스 스텁 생성
            RagServiceGrpc.RagServiceBlockingStub stub = RagServiceGrpc.newBlockingStub(channel);

            /* 지금(2024/06/08현재) 배포된 rag-search-api는 위도/경도가 바뀌어 있어서.. 수정된 버전 올라 가고 나면 요 기본값으로 변경해 줘야 함. */
            /* 틀린값 */
//            if(longitude == null)
//                longitude = 37.5287998F;
//
//            if(latitude == null)
//                latitude = 126.9687187F;
            /* 맞는값 */
            if(longitude == null)
                longitude = 126.9687187F;

            if(latitude == null)
                latitude = 37.5287998F;

            Location location = new Location((float) Math.round(longitude * 100) /100, (float) Math.round(latitude*100)/100);

            String dateTime = DateUtil.convertToShortStringByMS(System.currentTimeMillis());

            if(targetDate == null || targetDate.isBlank()) {
                targetDate = dateTime.substring(0,8);
            }

            if(targetTime == null || targetTime.isBlank()) {
                targetTime = dateTime.substring(8,10) + "00";
            }
            // FcstRequest 생성
            Rag.FcstRequest request = Rag.FcstRequest.newBuilder()
                    .setLongitude(longitude)
                    .setLatitude(latitude)
                    .setTargetDate(targetDate)
                    .setTargetTime(targetTime)
                    .build();

            // 서버로 요청 보내기

            Rag.FcstResponse response = stub.withDeadlineAfter(5, TimeUnit.SECONDS).weatherForecast(request);

            // 응답 처리
            rtnString = "시간:" + response.getTime() + "\n" + "지역:" + response.getArea();

            for (Rag.FcstResponse.FcstInfo info : response.getInfoList()) {
                System.out.println("Field: " + info.getField() + ", Value: " + info.getValue());
                switch (info.getField()) {
                    case ("1시간 기온") : // Field: 1시간 기온, Value: 21℃
                        rtnString += "\n온도:" + info.getValue();
                        break;
                    case ("풍속(동서성분)") : //Field: 풍속(동서성분), Value: 0.2m/s
                        break;
                    case ("풍속(남북성분)") : //Field: 풍속(남북성분), Value: 0.2m/s
                        break;
                    case ("풍향") : //Field: 풍향, Value: SW
                        break;
                    case ("풍속") ://Field: 풍속, Value: 0.3m/s
                        break;
                    case ("하늘상태") ://Field: 하늘상태, Value: 맑음
                        rtnString += "\n하늘상태:" + info.getValue();
                        break;
                    case ("강수 형태") ://Field: 강수 형태, Value: 강수 없음
                        break;
                    case ("강수 확률") ://Field: 강수 확률, Value: 0%
                        rtnString += "\n강수 확률:" + info.getValue();
                        break;
                    case ("파고") ://Field: 파고, Value: 0M
                        break;
                    case ("1시간 강수량") ://Field: 1시간 강수량, Value: 강수없음
                        break;
                    case ("습도") ://Field: 습도, Value: 75%
                        rtnString += "\n습도:" + info.getValue();
                        break;
                    case ("1시간 신적설") ://Field: 1시간 신적설, Value: 적설없음
                        break;
                    case ("현재 자외선 수치") ://Field: 현재 자외선 수치, Value: 0 (낮음)
                        rtnString += "\n자외선 수치:" + info.getValue();
                        break;
                    case ("초미세먼지 1시간 등급") ://Field: 초미세먼지 1시간 등급, Value: 좋음
                        rtnString += "\n초미세먼지 등급:" + info.getValue();
                        break;
                    case ("미세먼지 24시간 예측이동농도") ://Field: 미세먼지 24시간 예측이동농도, Value: 27
                        break;
                    case ("아황산가스 농도") ://Field: 아황산가스 농도, Value: 0.003
                        break;
                    case ("미세먼지 1시간 등급") ://Field: 미세먼지 1시간 등급, Value: 좋음
                        rtnString += "\n미세먼지 등급:" + info.getValue();
                        break;
                    case ("오존 지수") ://Field: 오존 지수, Value: 보통
                        rtnString += "\n오존 지수:" + info.getValue();
                        break;
                    case ("미세먼지 농도") ://Field: 미세먼지 농도, Value: 22
                        break;
                    case ("통합대기환경지수") ://Field: 통합대기환경지수, Value: 보통
                        rtnString += "\n통합대기환경 지수:" + info.getValue();
                        break;
                    case ("초미세먼지 농도") ://Field: 초미세먼지 농도, Value: 13
                        break;
                    case ("이산화질소 농도") ://Field: 이산화질소 농도, Value: 0.018
                        break;
                    case ("아황산가스 지수") ://Field: 아황산가스 지수, Value: 좋음
                        rtnString += "\n아황산가스 지수:" + info.getValue();
                        break;
                    case ("통합대기환경수치") ://Field: 통합대기환경수치, Value: 58
                        break;
                    case ("일산화탄소 농도") ://Field: 일산화탄소 농도, Value: 0.4
                        break;
                    case ("이산화질소 지수") ://Field: 이산화질소 지수, Value: 좋음
                        rtnString += "\n이산화질소 지수:" + info.getValue();
                        break;
                    case ("초미세먼지 24시간 예측이동농도") ://Field: 초미세먼지 24시간 예측이동농도, Value: 15
                        break;
                    case ("초미세먼지 24시간 등급") ://Field: 초미세먼지 24시간 등급, Value: 좋음
//                        rtnString += "\n초미세먼지 등급:" + info.getValue();
                        break;
                    case ("일산화탄소 지수") ://Field: 일산화탄소 지수, Value: 좋음
                        rtnString += "\n일산화탄소 지수:" + info.getValue();
                        break;
                    case ("미세먼지 24시간 등급") ://Field: 미세먼지 24시간 등급, Value: 좋음
//                        rtnString += "\n미세먼지 등급:" + info.getValue();
                        break;
                    case ("오존 농도") ://Field: 오존 농도, Value: 0.040
                        rtnString += "\n오존 농도:" + info.getValue();
                        break;
                    default:
                        break;
                }

//                if (info.getField().equals("1시간 기온"))
//                    rtnString += "\n온도:" + info.getValue();
//                else if (info.getField().equals("습도"))
//                    rtnString += "\n습도:" + info.getValue();
//                else if (info.getField().equals("강수 확률"))
//                    rtnString += "\n강수확률:" + info.getValue();
//                else if (info.getField().equals("하늘상태"))
//                    rtnString += "\n하늘상태:" + info.getValue();
            }
            channel.shutdown();
            // 채널 종료
        } catch (Exception e) {
            log.error("GRPC.WEATHER:" + e.getMessage());
        }

        return rtnString;

    }
}
