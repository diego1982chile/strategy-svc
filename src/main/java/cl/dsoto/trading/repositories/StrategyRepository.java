package cl.dsoto.trading.repositories;


import cl.dsoto.trading.entities.BarEntity;
import cl.dsoto.trading.entities.StrategyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by root on 13-10-22.
 */
public interface StrategyRepository extends JpaRepository<StrategyEntity, Long> {

    StrategyEntity findById(long id);
}
