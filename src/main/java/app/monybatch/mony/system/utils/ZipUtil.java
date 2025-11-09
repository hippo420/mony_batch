package app.monybatch.mony.system.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
@Slf4j
public class ZipUtil {
    private static final int BUFFER_SIZE = 4096;

    /**
     * ZIP 파일의 압축을 지정된 디렉터리에 해제합니다.
     * * @param zipFilePath 압축을 풀 ZIP 파일의 전체 경로
     * @param destDirectory 압축 해제된 파일이 저장될 디렉터리 경로
     * @throws IOException 입출력 오류 발생 시
     */
    public static String unzip(String zipFilePath, String destDirectory) throws IOException {

        // 1. 대상 디렉터리가 존재하지 않으면 생성합니다.
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        // 2. ZipInputStream을 사용하여 ZIP 파일 내용을 읽습니다.
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();

            // 3. ZIP 파일의 각 엔트리(파일 또는 디렉터리)를 순회합니다.
            while (entry != null) {
                // 압축 해제할 파일의 전체 경로
                String filePath = destDirectory + File.separator + entry.getName();

                if (!entry.isDirectory()) {
                    // 파일인 경우: 압축을 해제하고 파일로 저장합니다.
                    extractFile(zipIn, filePath);
                } else {
                    // 디렉터리인 경우: 해당 디렉터리를 생성합니다.
                    File dir = new File(filePath);
                    dir.mkdir();
                }

                // 다음 엔트리로 이동합니다.
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }

        log.info("✅ 압축 해제 완료: [{}] -> [{}]", zipFilePath, destDirectory);
        return destDirectory;
    }

    /**
     * 현재 ZipInputStream에서 읽은 파일 데이터를 디스크에 저장합니다.
     * * @param zipIn ZipInputStream 객체
     * @param filePath 저장할 파일의 전체 경로
     * @throws IOException 입출력 오류 발생 시
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        // 부모 디렉터리가 존재하지 않으면 생성 (하위 디렉터리 내 파일 처리 시 필요)
        File newFile = new File(filePath);
        new File(newFile.getParent()).mkdirs();

        // FileOutputStream을 사용하여 데이터를 파일로 씁니다.
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                fos.write(bytesIn, 0, read);
            }
        }
    }

}
