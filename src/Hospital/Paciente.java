package Hospital;

import java.util.Objects;

public class Paciente {

    private final String documento;
    private final String nombre;

    public Paciente(String documento, String nombre) {
        this.documento = Objects.requireNonNull(documento, "El documento es obligatorio");
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paciente)) return false;
        return documento.equals(((Paciente) o).documento);
    }

    @Override
    public int hashCode() {
        return documento.hashCode();
    }

    @Override
    public String toString() {
        return "Paciente{documento=" + documento + ", nombre=" + nombre + "}";
    }
}