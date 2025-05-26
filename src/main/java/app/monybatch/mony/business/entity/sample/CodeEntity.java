package app.monybatch.mony.business.entity.sample;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CODE_ENTITY")
@Getter
@Setter
@Table(name = "CODE_ENTITY", schema = "BATCHDATA")
public class CodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String item;

    private String itemNm;

    private Integer qty;

    private Integer amt;

    private String prc_yn;
}
