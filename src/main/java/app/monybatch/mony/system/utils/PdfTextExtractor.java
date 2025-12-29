package app.monybatch.mony.system.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;


public class PdfTextExtractor {
    public static String extractText(File file) throws Exception {
        Tika tika = new Tika();
        // PDF 파일에서 텍스트만 추출 (이미지 속 글자는 OCR 설정 필요)
        return tika.parseToString(file);
    }

    public static String extractTextFromPdf(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            // 표 데이터가 섞이지 않게 정렬 옵션을 켜는 것이 유리함
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("PDF 파싱 실패: " + pdfFile.getName(), e);
        }
    }
}
