package ai.maum.chathub.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class ScpUtil {
    private static final int PORT = 22; // 기본 SSH 포트

    public void transferFile(String username, String password, String host, String remoteDirectory, String localFilePath) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

        try {
            // 세션 생성 및 연결
            session = jsch.getSession(username, host, PORT);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // SCP 명령어 설정
            String command = "scp -t " + remoteDirectory;
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // 입출력 스트림 설정
            OutputStream out = channel.getOutputStream();
            channel.connect();

            // 파일 정보 및 파일 전송
            File localFile = new File(localFilePath);
            long fileSize = localFile.length();
            String commandInfo = "C0644 " + fileSize + " " + localFile.getName() + "\n";
            out.write(commandInfo.getBytes());
            out.flush();

            FileInputStream fis = new FileInputStream(localFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            fis.close();

            // 전송 완료 신호
            out.write(0);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("파일 전송 중 오류 발생", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public void deleteFile(String username, String password, String host, String remoteFilePath) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

        try {
            // 세션 생성 및 연결
            session = jsch.getSession(username, host, PORT);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 삭제 명령어 설정
            String command = "rm -f " + remoteFilePath;
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            InputStream in = channel.getInputStream();
            channel.connect();

            // 명령어 실행 결과 확인
            int exitStatus = channel.getExitStatus();
            if (exitStatus != 0) {
                throw new RuntimeException("파일 삭제 실패. exit-status: " + exitStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("파일 삭제 중 오류 발생", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
