package app.monybatch.mony.business.entity.sample;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name="ASIS_ENTITY")
public class AsIsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String item;

    private String itemNm;

    private BigDecimal price;

}
