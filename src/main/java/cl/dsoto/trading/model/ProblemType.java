package cl.dsoto.trading.model;

import java.io.Serializable;

/**
 * @author Andrés Farías on 8/23/16.
 */
public enum ProblemType implements Serializable {

    BINARY(1, "Codificacón binaria"),
    INTEGER(2, "Codificación entera"),
    REAL(3, "Codificación real");

    /** Identificador único de la base de datos */
    private long id;

    /** Nombre o descripción del cambio */
    private String name;

    ProblemType(long id, String name) {
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
    public static ProblemType valueOf(long idProblemType) {
        for (ProblemType problemType : values()) {
            if (problemType.getId() == idProblemType) {
                return problemType;
            }
        }

        throw new IllegalArgumentException("No hay un tipo de problema con ID=" + idProblemType);
    }

}

