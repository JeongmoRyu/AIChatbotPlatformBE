package ai.maum.chathub.external.api.n8n.service;

import ai.maum.chathub.api.chatplay.dto.req.ChatplayReq;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.entity.MemberEntity;
import ai.maum.chathub.api.member.mapper.MemberMapper;
import ai.maum.chathub.api.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class N8nService {

    private final MemberRepository memberRepository;

    private String N8N_ACCOUNT_ANSWERS = "{" +
            "  \"version\": \"v4\"," +
            "  \"personalization_survey_submitted_at\": \"2025-02-26T05:49:39.883Z\"," +
            "  \"personalization_survey_n8n_version\": \"1.77.0\"," +
            "  \"companySize\": \"<20\"," +
            "  \"companyType\": \"saas\"," +
            "  \"role\": \"business-owner\"," +
            "  \"reportedSource\": \"google\"" +
            "}";
    private String N8N_ACCOUNT_ROLE = "global:member";

    @Value("${service.n8n.jdbc-url}")
    String URL;
    @Value("${service.n8n.username}")
    String USERNAME;
    @Value("${service.n8n.password}")
    String PASSWORD;
    @Value("${service.n8n.driver-class-name}")
    String DRIVER_CLASS_NAME;

    public BaseResponse<Void> addAccount(List<ChatplayReq> userList) {


        log.info("db connect... : {},{},{},{}", URL, USERNAME, PASSWORD, DRIVER_CLASS_NAME);

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            connection.setAutoCommit(false);

            String n8nAccount = "INSERT INTO \"user\" (email, \"firstName\", password, \"personalizationAnswers\", role ) " +
                    "VALUES (?, ?, ?, ?::jsonb, ?) " +
                    "RETURNING id; ";

            String n8nProject = "INSERT INTO project (id, name, type) " +
                    "VALUES (?, ?, ?) ";

            String n8nProjectRelation = "INSERT INTO project_relation (\"projectId\", \"userId\", role) " +
                    "VALUES (?, ?, ?) ";


            for (ChatplayReq user : userList) {

                String email = user.getEmail();
                String name = user.getName();
                String password = generateN8nPassword(user.getEmail()); // n8n은 일단 userid = password

                try (PreparedStatement preparedStatement = connection.prepareStatement(n8nAccount)) {
                    preparedStatement.setString(1, email);
                    preparedStatement.setString(2, name);
                    preparedStatement.setString(3, password);
                    preparedStatement.setString(4, N8N_ACCOUNT_ANSWERS);
                    preparedStatement.setString(5, N8N_ACCOUNT_ROLE);

                    try( ResultSet resultAccount = preparedStatement.executeQuery()) {

                        UUID userId = null;
                        String projectId = generateNanoId();

                        if (resultAccount.next()) {
                            userId = (UUID) resultAccount.getObject("id");
                            log.debug("n8n Generated or Updated User ID: " + userId);
                        }

                        try (PreparedStatement preparedStatement1 = connection.prepareStatement(n8nProject)) {
                            preparedStatement1.setString(1, projectId);
                            preparedStatement1.setString(2, String.format("%s<%s>", name, email));
                            preparedStatement1.setString(3, "personal");

                            int resultProject = preparedStatement1.executeUpdate();
                            log.debug("n8n project insert :" + resultProject);
                        }

                        try (PreparedStatement preparedStatement2 = connection.prepareStatement(n8nProjectRelation)) {
                            preparedStatement2.setString(1, projectId);
                            preparedStatement2.setObject(2, userId);
                            preparedStatement2.setString(3, "project:personalOwner");

                            int resultProject = preparedStatement2.executeUpdate();
                            log.debug("n8n project relation insert :" + resultProject);
                        }

                        connection.commit();

                    }
                } catch (SQLException e) {
                        e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            log.error("Connection Failed! Check output console for details.", e);
//            throw new RuntimeException(e);
        }

        return BaseResponse.success();
    }

    private String generateN8nPassword(String rawPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10); // n8n과 동일한 cost factor
        return passwordEncoder.encode(rawPassword);
    }

    private String generateNanoId() {
        String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int ID_LENGTH = 16;
        SecureRandom RANDOM = new SecureRandom();

        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(randomIndex));
        }
        return sb.toString();
    }

    public BaseResponse<Void> syncAccount() {

        List<ChatplayReq> userList = memberRepository.findMemberEntitiesByUseYn("Y")
                .stream()
                .map(member -> new ChatplayReq(member.getUsername(), member.getName()))
                .collect(Collectors.toList());



        for(ChatplayReq user:userList) {
            String email = user.getEmail();
            String name = user.getName();

            log.debug("member:{},{}", email, name);

        }

        return addAccount(userList);

        // n8n sync process
//        return BaseResponse.success("n8n sync success");
    }
}
