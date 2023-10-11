package cl.dsoto.trading.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;

/**
 * Created by root on 03-05-23.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "BAR")
public class BarEntity extends AbstractPersistableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    double open;
    double high;
    double low;
    double close;
    double volume;
    ZonedDateTime beginTime;
    ZonedDateTime endTime;

    @Override
    public Long getId() {
        return id;
    }
}
