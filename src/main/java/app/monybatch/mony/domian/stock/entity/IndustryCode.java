package app.monybatch.mony.domian.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "industry_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndustryCode {

    @Id
    @Column(name = "code", length = 5, nullable = false)
    private String code;

    @Column(name = "codename", length = 250)
    private String codename;

    @Column(name = "mainCatCode", length = 1)
    private String mainCatCode;

    @Column(name = "mainCatName", length = 100)
    private String mainCatName;

    @Column(name = "midCatCode", length = 2)
    private String midCatCode;

    @Column(name = "midCatName", length = 100)
    private String midCatName;

    @Column(name = "subCatCode", length = 3)
    private String subCatCode;

    @Column(name = "subCatName", length = 100)
    private String subCatName;

    @Column(name = "detailCatCode", length = 4)
    private String detailCatCode;

    @Column(name = "detailCatName", length = 100)
    private String detailCatName;
}