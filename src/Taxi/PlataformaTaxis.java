package Taxi;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Plataforma de solicitud de taxis.
 *
 * Una sola estructura: LinkedHashSet<SolicitudDeViaje> con las solicitudes PENDIENTES.
 *   - orden de inserción  -> el primer elemento es siempre la solicitud más antigua
 *   - add / remove O(1)   -> registrar y cancelar por id sin recorrer
 *   - solo pendientes     -> al atender o cancelar, la solicitud se QUITA del set,
 *                            así "mostrar pendientes" no recorre el histórico
 */
public class PlataformaTaxis {

    private final Set<SolicitudDeViaje> pendientes = new LinkedHashSet<>();

    /**
     * Registra una solicitud de viaje.
     */
    public boolean registrar(SolicitudDeViaje solicitud) {
        return pendientes.add(solicitud);
    }

    /**
     * Atiende la solicitud más antigua y la quita de las pendientes.
     */
    public SolicitudDeViaje atenderSiguiente() {
        Iterator<SolicitudDeViaje> it = pendientes.iterator();
        if (!it.hasNext()) {
            return null;
        }
        SolicitudDeViaje solicitud = it.next();
        it.remove();
        solicitud.setEstado(SolicitudDeViaje.Estado.ATENDIDA);
        return solicitud;
    }

    /**
     * Cancela una solicitud pendiente a partir de su id.
     */
    public boolean cancelar(String id) {
        return pendientes.remove(new SolicitudDeViaje(id));
    }

    /** Vista de solo lectura de las pendientes, de la más antigua a la más reciente. */
    public Collection<SolicitudDeViaje> pendientes() {
        return Collections.unmodifiableCollection(pendientes);
    }

    public int totalPendientes() {
        return pendientes.size();
    }
}
