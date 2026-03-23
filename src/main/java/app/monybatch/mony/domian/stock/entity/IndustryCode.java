package app.monybatch.mony.domian.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "industry_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndustryCode {

    @Id
    @Column(name = "code", length = 30, nullable = false)
    private String code;

    @Column(name = "code_name", length = 100, nullable = false)
    private String codeName;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "etc1", length = 100)
    private String etc1;

    @Column(name = "etc2", length = 100)
    private String etc2;

    @Column(name = "etc3", length = 100)
    private String etc3;

    @Column(name = "etc4", length = 100)
    private String etc4;

    @OneToMany(mappedBy = "industryCode")
    private List<Stock> stocks;


}