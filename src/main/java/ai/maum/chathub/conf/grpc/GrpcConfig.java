package ai.maum.chathub.conf.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rag_service.rag_module.RagServiceGrpc;
import rag_service.rag_module.RagServiceGrpc.RagServiceStub;


@Configuration
public class GrpcConfig {
    @Value("${service.grpc.chat.host}")
    private String host;

    @Value("${service.grpc.chat.port}")
    private int port;

//    private ManagedChannel managedChannel;

//    @Bean
//    public ManagedChannel managedChannel() {
//        managedChannel = ManagedChannelBuilder.forAddress(host, port)
//                .usePlaintext()
//                .build();
//        return managedChannel;
//    }

    @Bean
    public RagServiceStub ragClient () {
        return RagServiceGrpc.newStub(ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build());
    }
}
