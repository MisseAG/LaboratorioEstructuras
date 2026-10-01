package Hospital;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistroPacientes {

    private final Map<String, Paciente> pacientes = new LinkedHashMap<>();

    /** Registra un paciente al llegar */
    public boolean registrar(Paciente paciente) {
        // putIfAbsent devuelve null solo cuando la clave no exista
        // documento como key, paciente como value
        return pacientes.putIfAbsent(paciente.getDocumento(), paciente) == null;
    }

    public Paciente buscarPorDocumento(String documento) {
        return pacientes.get(documento);
    }

    public boolean existe(String documento) {
        return pacientes.containsKey(documento);
    }

    public int total() {
        return pacientes.size();
    }

    public Collection<Paciente> pacientesEnOrdenDeLlegada() {
        return Collections.unmodifiableCollection(pacientes.values());
    }
}
