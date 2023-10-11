package cl.dsoto.trading.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Created by root on 10-11-22.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@MappedSuperclass
public abstract class AbstractPersistableEntity<T extends Serializable> implements Persistable<T> {


    @Override
    public boolean isNew() {
        return getId() == null;
    }


    public boolean isPersisted() {
        return !isNew();
    }
}
