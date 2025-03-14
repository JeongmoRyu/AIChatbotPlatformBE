package ai.maum.chathub.api.chat;

import ai.maum.chathub.api.chat.handler.ChatGrpcConnectionHandler;
import ai.maum.chathub.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import rag_service.rag_module.Rag.Message;

/*
import rag_service.rag_module.Message;
import rag_service.rag_module.RagServiceGrpc;
 */


@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name="채팅/Proto", description="Proto이용 채팅 API")
public class ProtoController {

    private final ChatGrpcConnectionHandler connectionHandler;

    @Operation(summary = "채팅/Proto-Test", description = "채팅/Proto-Test")
    @PostMapping({"/maum-admin/grpc/test"})
    public String protoTest(
            @RequestParam(value = "host", defaultValue = "i-dev-mcl-rag-search.apddev.com") String host,
            @RequestParam(value = "port", defaultValue = "443") Integer port,
            @RequestParam(value = "message", defaultValue = "hello grpc!!!") String message
    ) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // Insecure channel for simplicity (use SSL/TLS in production)
                .build();

        rag_service.rag_module.RagServiceGrpc.RagServiceBlockingStub stub = rag_service.rag_module.RagServiceGrpc.newBlockingStub(channel);
        Message requestMessage = Message.newBuilder()
                .setMsg(message)
                .build();

        Message responseMessage = stub.echo(requestMessage);

        LogUtil.debug("Response from grpc server: " + responseMessage.getMsg());

        return responseMessage.getMsg();
    }
}
/*
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import rag_service.rag_module.Message;
import rag_service.rag_module.RagServiceGrpc;

public class GrpcClient {

    public static void main(String[] args) {
        // Set up the channel to connect to the gRPC server
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() // Insecure channel for simplicity (use SSL/TLS in production)
                .build();

        // Create a gRPC client stub
        RagServiceGrpc.RagServiceBlockingStub stub = RagServiceGrpc.newBlockingStub(channel);

        // Create a Message to send
        Message requestMessage = Message.newBuilder()
                .setMsg("Hello from Java gRPC client!")
                .build();

        // Call the Echo RPC
        Message responseMessage = stub.echo(requestMessage);

        // Print the response
        System.out.println("Response from server: " + responseMessage.getMsg());

        // Shutdown the channel
        channel.shutdown();
    }
}
 */
