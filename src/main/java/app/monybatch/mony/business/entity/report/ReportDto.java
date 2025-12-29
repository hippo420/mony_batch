package app.monybatch.mony.business.entity.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
@Setter
public class ReportDto {


    private String pubymd;
    private String company;
    private String item;
    private String itemName;
    private String title;
    private String content;
    private BigDecimal targetPrice;
    private Invest Type;
    private String invest;
    private String url;
    private String pdfUrl;
    private String pdfFilename;

    public ReportDto ()
    {

    }
    public ReportDto (String pubymd, String item, String itemName, String url,String pdfUrl,String pdfFilename)
    {
        this.pubymd=pubymd;
        this.item=item;
        this.itemName=itemName;
        this.url=url;
        this.pdfUrl=pdfUrl;
        this.pdfFilename=pdfFilename;
    }

}
