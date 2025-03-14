package ai.maum.chathub.api.file.service;

import ai.maum.chathub.api.file.repo.SourceFileRepository;
import ai.maum.chathub.util.FileUtil;
import ai.maum.chathub.api.file.entity.SourceFileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ImageFileService {
   private final SourceFileRepository sourceFileRepository;

    Color[] colors = {
            new Color(255, 182, 193), // Light Pink
            new Color(135, 206, 250), // Light Sky Blue
            new Color(144, 238, 144), // Light Green
            new Color(255, 160, 122), // Light Salmon
            new Color(255, 255, 102)  // Light Yellow
    };

    public ResponseEntity<Resource> generateImage(String text) {
        // 문자열의 첫 번째 문자 추출 (알파벳, 한글 등 모든 유니코드 문자 포함)
        String firstChar = text.substring(0, 1);

        // 이미지 생성
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 색상 중에서 랜덤으로 선택
        Random rand = new Random();
        Color randomColor = colors[rand.nextInt(colors.length)];  // 배열에서 랜덤 색상 선택
        g2d.setColor(randomColor);
        g2d.fillRect(0, 0, 100, 100);

        try {
            Resource fontResource = new ClassPathResource("font/Pretendard-Regular.otf"); // OTF 폰트 경로
            InputStream fontStream = fontResource.getInputStream();
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(48f);
            g2d.setFont(customFont);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            // 폰트 로드에 실패하면 기본 폰트 사용
            g2d.setFont(new Font("Serif", Font.BOLD, 48));
        }

        // 텍스트 색상과 폰트 설정
        g2d.setColor(Color.BLACK);
        // 텍스트 그리기
        g2d.drawString(firstChar, 25, 75);
        g2d.dispose();

        // 이미지를 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 바이트 배열 리소스로 변환
        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

        // ResponseEntity로 반환
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=image.png");
        headers.add(HttpHeaders.CONTENT_TYPE, "image/png");

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    public ResponseEntity<Resource> getImage(Integer fileId) {
        try {
            Optional<SourceFileEntity> imgFileOptional = sourceFileRepository.findById(fileId);

            // 이미지 파일이 없는 경우 기본 이미지 반환
            if (imgFileOptional.isEmpty()) {
                return getDefaultImage();
            }

            SourceFileEntity imgFileEntity = imgFileOptional.get();

            String imagePath = imgFileEntity.getPath();
            Path path = Paths.get(imagePath);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                // 파일의 MIME 타입을 결정
                String contentType = FileUtil.getContentType(path.getFileName().toString());

                // ResponseEntity를 통해 이미지 파일을 반환
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                // 파일이 없거나 읽을 수 없는 경우 기본 이미지 반환
                return getDefaultImage();
            }

        } catch (Exception e) {
            // 예외 발생 시 기본 이미지 반환
            return getDefaultImage();
        }
    }

    // 기본 이미지를 반환하는 함수
    public ResponseEntity<Resource> getDefaultImage() {
        try {
            // resources 폴더에 있는 기본 이미지 로드
            Resource defaultImage = new ClassPathResource("image/ex.png");

            // 기본 이미지 파일이 존재하는 경우
            if (defaultImage.exists() && defaultImage.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ex.png\"")
                        .body(defaultImage);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            // 기본 이미지 파일을 읽는 중 오류 발생 시 500 반환
            return ResponseEntity.status(500).build();
        }
    }
}
