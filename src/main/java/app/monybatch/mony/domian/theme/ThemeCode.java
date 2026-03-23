package app.monybatch.mony.domian.theme;

import app.monybatch.mony.domian.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "theme_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeCode {

    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;

    @Column(name = "detail", length = 300)
    private String detail;

    @Column(name = "use_yn", length = 1)
    private String useYn = "Y";

    @OneToMany(mappedBy = "themeCode", fetch = FetchType.LAZY)
    private List<Stock> stocks;
}