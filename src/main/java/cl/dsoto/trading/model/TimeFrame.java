package cl.dsoto.trading.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Andrés Farías on 8/23/16.
 */
@NoArgsConstructor
public enum TimeFrame implements Serializable {

    MINUTE(1, "Minute"),
    HOUR(2, "Hour"),
    DAY(3, "Day"),
    WEEK(4, "Week"),
    MONTH(5, "Month"),
    YEAR(6, "Year");

    /** Identificador único de la base de datos */
    private long id;

    /** Nombre o descripción del cambio */
    private String name;

    TimeFrame(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Este método es responsable de retornar el AuditActionType asociado al ID <code>idAuditActionType</code>.
     *
     * @param idProblemType El identificador del AuditActionType.
     *
     * @return El objeto que representa la acción de auditoría.
     */
    public static TimeFrame valueOf(long idProblemType) {
        for (TimeFrame problemType : values()) {
            if (problemType.getId() == idProblemType) {
                return problemType;
            }
        }

        throw new IllegalArgumentException("No hay un tipo de problema con ID=" + idProblemType);
    }

}

