package ai.maum.chathub.external.api.kimm.service;

import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class KimmService {

    @Value("${spring.second-datasource.jdbc-url}")
    String URL;
    @Value("${spring.second-datasource.username}")
    String USERNAME;
    @Value("${spring.second-datasource.password}")
    String PASSWORD;
    @Value("${spring.second-datasource.driver-class-name}")
    String DRIVER_CLASS_NAME;
    @Value("${spring.second-datasource.schema}")
    String SCHEMA;
    @Value("${spring.second-datasource.db-link}")
    String DB_LINK;

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${service.chatplay.enable}")
    Boolean CHATPLAY_ENABLE;
    @Value("${service.chatplay.jdbc-url}")
    String CHATPLAY_URL;
    @Value("${service.chatplay.username}")
    String CHATPLAY_USERNAME;
    @Value("${service.chatplay.password}")
    String CHATPLAY_PASSWORD;
    @Value("${service.chatplay.driver-class-name}")
    String CHATPLAY_DRIVER_CLASS_NAME;

    // 매일 새벽 2시 실행
    @Scheduled(cron = "0 0 2 * * ?")
//    @Scheduled(cron = "0 28 15 * * ?")
    public void executeUserSync() {

//        log.debug("{} {} {}", URL, USERNAME, PASSWORD);
        log.info("start user sync...");

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        Connection connection2 = null;
        PreparedStatement statement2 = null;
        ResultSet resultSet2 = null;

        try {

            log.info("db connect... : {},{},{},{},{},{}", URL, USERNAME, PASSWORD, SCHEMA, DRIVER_CLASS_NAME, DB_LINK);

            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            if(CHATPLAY_ENABLE)
                connection2 = DriverManager.getConnection(CHATPLAY_URL, CHATPLAY_USERNAME, CHATPLAY_PASSWORD);

            String query = "SELECT USER_ID, USER_NAME, PASSWD, HOLD_OFFI, HOLD_OFFI_NM, \"ROLE\"\n" +
                           "FROM " + SCHEMA + ".VI_USER_INFO" + (DB_LINK==null || DB_LINK.isEmpty() ? "" : "@" + DB_LINK);
            log.info("Query: " + query);

            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();

            while(resultSet.next()) {
                log.info("DATA:" + resultSet.getString(1));
                MemberDetail memberDetail = new MemberDetail();

                String email = resultSet.getString(1);
                String name = resultSet.getString(2);
                String password = passwordEncoder.encode(resultSet.getString(3));

                memberDetail.setUsername(email);
                memberDetail.setName(name);
                memberDetail.setPassword(password);
                memberDetail.setRoles("ROLE_USER");

                int result = memberMapper.upsertMember(memberDetail);

                if (CHATPLAY_ENABLE && connection2 != null && !connection2.isClosed()) {
                    String query2 = "INSERT INTO chatplay.user (id, email, name) " +
                            "VALUES (UUID(), ?, ?) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "name = VALUES(name);";

                    try (PreparedStatement preparedStatement = connection2.prepareStatement(query2)) {

                        preparedStatement.setString(1, email); // 첫 번째 ?에 email 값을 바인딩
                        preparedStatement.setString(2, name);  // 두 번째 ?에 name 값을 바인딩

                        int rowsAffected = preparedStatement.executeUpdate();
                        System.out.println("Rows affected: " + rowsAffected);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                log.info("result: {} {}", memberDetail.getUsername(), memberDetail.getName());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

