package app.monybatch.mony.business.repository.jpa;

import app.monybatch.mony.business.entity.sample.CodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<CodeEntity,Long> {

}
