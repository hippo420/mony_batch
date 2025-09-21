package app.monybatch.mony.business.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@MappedSuperclass
@Getter @Setter @ToString
public class CommonEntitiy {

    @JsonProperty("REG_DATE")
    @Column(name = "reg_date",length = 14)
    private LocalDateTime regDate;

    @JsonProperty("UPD_DATE")
    @Column(name = "upd_date",length = 14)
    private LocalDateTime updDate;

    public void setRegDate(LocalDateTime regDate) {
        if(regDate != null) {
            this.regDate = regDate;
        }else{
            this.regDate = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        }
    }

    public void setUpdDate(LocalDateTime updDate) {
        if(updDate != null) {
            this.updDate = updDate;
        }else{
            this.regDate = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        }

    }
}
