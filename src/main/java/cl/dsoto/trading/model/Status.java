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
public enum Status implements Serializable {

    NEW(1, "New"),
    CREATED(2, "Created"),
    PROCESSING(3, "Processing"),
    COMPLETED(4, "Completed"),
    ABORTED(5, "Aborted");

    /** Identificador único de la base de datos */
    private long id;

    /** Nombre o descripción del cambio */
    private String name;

    Status(long id, String name) {
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
     * @param statusId El identificador del AuditActionType.
     *
     * @return El objeto que representa la acción de auditoría.
     */
    public static Status valueOf(long statusId) {
        for (Status problemType : values()) {
            if (problemType.getId() == statusId) {
                return problemType;
            }
        }

        throw new IllegalArgumentException("There's no status with ID=" + statusId);
    }

}

