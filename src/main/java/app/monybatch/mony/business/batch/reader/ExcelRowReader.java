package app.monybatch.mony.business.batch.reader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.batch.item.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class ExcelRowReader implements ItemStreamReader<Row>
{

    private final String filepath;
    private FileInputStream fi;
    private Workbook wb;
    private Iterator<Row> rowCursor;
    private int currentRow;
    private final String ROW_KEY ="current.row.number";

    public ExcelRowReader(String filepath, int startRow){
        this.filepath = filepath;
        this.currentRow = startRow;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try{
            fi = new FileInputStream(filepath);
            wb = WorkbookFactory.create(fi);
            Sheet sheet = wb.getSheetAt(0);
            this.rowCursor = sheet.rowIterator();


            if(executionContext.containsKey(ROW_KEY)){
                currentRow  = executionContext.getInt(ROW_KEY);
            }

            for(int i =0;i< currentRow && rowCursor.hasNext();i++)
            {
                rowCursor.next();
            }
        }catch(IOException e){
            throw new ItemStreamException(e);
        }
    }

    @Override
    public Row read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if(rowCursor != null && rowCursor.hasNext()){
            currentRow++;
            return rowCursor.next();
        }else{
            return null;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(ROW_KEY,currentRow);
    }

    @Override
    public void close() throws ItemStreamException {
        try{
            if(wb != null)
                wb.close();

            if(fi != null)
                fi.close();

        }catch(IOException e){
            throw  new ItemStreamException(e);
        }
    }
}
