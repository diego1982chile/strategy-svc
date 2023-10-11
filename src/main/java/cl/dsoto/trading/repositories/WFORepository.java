package cl.dsoto.trading.repositories;

import cl.dsoto.trading.entities.BackTestEntity;
import cl.dsoto.trading.entities.WFOEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by root on 13-10-22.
 */
public interface WFORepository extends JpaRepository<WFOEntity, Long> {

    WFOEntity findById(long id);

    @Query("SELECT w FROM WFOEntity w order by w.id desc")
    List<WFOEntity> findAllOrderByIdDesc();
}
