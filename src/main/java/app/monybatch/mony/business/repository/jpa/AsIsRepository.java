package app.monybatch.mony.business.repository.jpa;
import app.monybatch.mony.business.entity.sample.AsIsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsIsRepository extends JpaRepository<AsIsEntity,Long> {
}
