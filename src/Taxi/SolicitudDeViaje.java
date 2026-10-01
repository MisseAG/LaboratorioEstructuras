package Taxi;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Solicitud de viaje de la plataforma de taxis.
 */
public class SolicitudDeViaje {

    public enum Estado { PENDIENTE, ATENDIDA, CANCELADA }

    private final String id;
    private final LocalDateTime hora;
    private final String lugar;
    private Estado estado;

    public SolicitudDeViaje(String id, LocalDateTime hora, String lugar) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.hora = hora;
        this.lugar = lugar;
        this.estado = Estado.PENDIENTE;
    }

    /**
     * Solicitud "de consulta": solo lleva el id. Sirve para quitar una solicitud de un
     */
    public SolicitudDeViaje(String id) {
        this(id, null, null);
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getHora() {
        return hora;
    }

    public String getLugar() {
        return lugar;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolicitudDeViaje)) return false;
        return id.equals(((SolicitudDeViaje) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "SolicitudDeViaje{id=" + id + ", hora=" + hora + ", lugar=" + lugar + ", estado=" + estado + "}";
    }
}