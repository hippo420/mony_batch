package app.monybatch.mony.business.entity.report;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(uniqueConstraints =
        {
            @UniqueConstraint(
                    name = "uk_report",
                    columnNames = {"basymd", "company","item"} // 순서가 중요!
            )
        },
        indexes = {
                @Index(name = "idx_basymd", columnList = "basymd"),
                @Index(name = "idx_company", columnList = "company"),
                @Index(name = "idx_item", columnList = "item")
        }
      )
public class Report {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String basymd;
    private String company;
    private String item;
    private String itemName;
    private String title;
    private BigDecimal price;
    private Invest Type;
    private String invest;
    @Column(length = 2000)
    private String pdfUrl;
    private String pdfFilename;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
}
