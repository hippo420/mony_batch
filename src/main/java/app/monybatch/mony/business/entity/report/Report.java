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
    private Long id;

    private String basymd;
    private String company;
    private String item;
    private String itemName;
    private BigDecimal price;
    private Invest Type;
}
