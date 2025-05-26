package app.monybatch.mony.business.batch.writer;


import app.monybatch.mony.business.entity.sample.ExcelEntity;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelRowWriter implements ItemStreamWriter<ExcelEntity>
{

    private final String filepath;
    private FileInputStream fi;
    private Workbook wb;
    private Sheet sheet;
    private int currentRow;
    private int currentCol;

    public ExcelRowWriter(String filepath, int startRow, int endCol){
        this.filepath = filepath;
        this.currentRow = startRow;
        this.currentCol = endCol;
    }

    @Override
    public void write(Chunk<? extends ExcelEntity> chunk) throws Exception {
        for(ExcelEntity entity: chunk){
            Row row = sheet.createRow(currentRow++);

            row.createCell(0).setCellValue(entity.getIdxNm());
            row.createCell(1).setCellValue(String.valueOf(entity.getClosePrice()));
            row.createCell(2).setCellValue(String.valueOf(entity.getComparison()));
            row.createCell(3).setCellValue(String.valueOf(entity.getFRate()));
            row.createCell(4).setCellValue(String.valueOf(entity.getOpenPrice()));
            row.createCell(5).setCellValue(String.valueOf(entity.getUpperPrice()));
            row.createCell(6).setCellValue(String.valueOf(entity.getLowerPrice()));
            row.createCell(7).setCellValue(String.valueOf(entity.getVolume()));
            row.createCell(8).setCellValue(String.valueOf(entity.getTranPrice()));
            row.createCell(9).setCellValue(String.valueOf(entity.getMktCapital()));

        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        wb = new XSSFWorkbook();
        sheet = wb.createSheet("지수정보");
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        ItemStreamWriter.super.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        try(FileOutputStream fos = new FileOutputStream(filepath)){
            wb.write(fos);
        }catch (IOException e) {
            throw new ItemStreamException(e);
        } finally {
            try{
                wb.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
