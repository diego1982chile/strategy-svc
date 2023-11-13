package cl.dsoto.trading.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by des01c7 on 22-03-19.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Optimization implements Serializable {
    /** El identificador único de la entidad, inicialmente fijado en <code>NON_PERSISTED_ID</code>. */
    private long id;

    private Strategy strategy;
    private Timestamp timestamp;

    private List<Objective> objectives;
    private List<Solution> solutions;

    public Optimization(Strategy strategy, Timestamp timestamp, List<Objective> objectives, List<Solution> solutions) {
        this.strategy = strategy;
        this.timestamp = timestamp;
        this.objectives = objectives;
        this.solutions = solutions;
    }
}
