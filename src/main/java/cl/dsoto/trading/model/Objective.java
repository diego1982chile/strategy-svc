package cl.dsoto.trading.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by des01c7 on 22-03-19.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Objective implements Serializable {
    /** El identificador único de la entidad, inicialmente fijado en <code>NON_PERSISTED_ID</code>. */
    private long id;

    private Optimization optimization;
    private double objective;


}
