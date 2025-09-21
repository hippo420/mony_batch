package app.monybatch.mony.business.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name ="info_stock_temp")
public class StockTemp extends BaseStock {
}
