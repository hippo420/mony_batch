package app.monybatch.mony.domian.stock.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name ="info_stock")
public class Stock extends BaseStock {
}
