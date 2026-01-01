package app.monybatch.mony.system.utils;

import app.monybatch.mony.system.core.constant.AppConst;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioUtil {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    private static final long PART_SIZE = 10 * 1024 * 1024L;

    /**
     * 로컬 File 객체를 받아 MinIO에 업로드합니다.
     * @param file 업로드할 실제 File 객체
     * @return 업로드된 파일의 조회 경로
     */
    @Async
    public void uploadFile(File file) {
        // 1. 파일 유효성 검증
        if (file == null || !file.exists()) {
            throw new RuntimeException("파일이 존재하지 않습니다.");
        }

        // 2. 저장될 파일명 생성
        String fileName = file.getName();

        try {
            //(중복 방지를 위해 타임스탬프 결합)
            if(!isExistFile(fileName, bucketName)) return;


            // 3. 파일의 ContentType 추출 (Files.probeContentType 활용)
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = AppConst.CONTENT_TYPE_STREAM; // 알 수 없는 경우 기본값
            }

            // 4. MinIO 업로드 (File 경로를 사용하는 uploadObject 사용)
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .filename(file.getAbsolutePath()) // File 객체의 절대 경로 전달
                            .contentType(contentType)
                            .build()
            );

            log.info("MinIO 업로드 성공: {} (원본명: {})", fileName, file.getName());
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생 - 파일명: {}, 에러: {}", file.getName(), e.getMessage());
            throw new RuntimeException("MinIO 업로드 실패", e);
        }
    }

    public boolean isExistFile(String fileName, String bucketName)
    {
        try
        {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.error("파일이 이미 존재합니다: {}", fileName);
            return false; // 파일이 존재하면 업로드하지 않고 종료

        } catch (ErrorResponseException e) {
            return true;
        }
        catch (Exception ignored)
        {

        }
        return true;
    }
}
