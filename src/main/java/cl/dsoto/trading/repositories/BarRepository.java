package cl.dsoto.trading.repositories;


import cl.dsoto.trading.entities.BarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by root on 13-10-22.
 */
public interface BarRepository extends JpaRepository<BarEntity, Long> {

    BarEntity findById(long id);
}
