package app.monybatch.mony.domian.dart.repository;

import app.monybatch.mony.domian.dart.DartBasicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DartRepository extends JpaRepository<DartBasicEntity,String> {
}
