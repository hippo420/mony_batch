package app.monybatch.mony.business.repository.jpa;


import app.monybatch.mony.business.entity.sample.ExcelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LargerScaleRepository extends JpaRepository<ExcelEntity,String> {

}
