package cl.dsoto.trading.repositories;

import cl.dsoto.trading.entities.BackTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by root on 13-10-22.
 */
public interface BackTestRepository extends JpaRepository<BackTestEntity, Long> {

    BackTestEntity findById(long id);
}
