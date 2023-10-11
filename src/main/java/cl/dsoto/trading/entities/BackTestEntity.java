package cl.dsoto.trading.entities;

import cl.dsoto.trading.model.Optimization;
import cl.dsoto.trading.model.ProblemType;
import cl.dsoto.trading.model.Solution;
import cl.dsoto.trading.model.TimeFrame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import ta4jexamples.strategies.*;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by des01c7 on 29-03-19.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "BACK_TEST")
public class BackTestEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String name;
    Timestamp timestamp;
    Date start;
    Date end;

    int timeFrame;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    List<OptimizationEntity> optimizations = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    List<BarEntity> bars = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }
}
