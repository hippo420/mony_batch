package app.monybatch.mony.business.repository.jpa;


import app.monybatch.mony.business.entity.sample.TobeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TobeRepository extends JpaRepository<TobeEntity,Long> {

}
