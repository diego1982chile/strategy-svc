package cl.dsoto.trading.entities;

import cl.dsoto.trading.model.Status;
import cl.dsoto.trading.model.TimeFrame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 03-10-23.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "WFO")
public class WFOEntity extends AbstractPersistableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String name;

    Integer timeFrame;

    Integer step;

    Integer iterations;

    LocalDate start;
    LocalDate end;

    Double inSample;
    Double outSample;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    List<WFORecordEntity> wfoRecords = new ArrayList<>();

    long status;
}
