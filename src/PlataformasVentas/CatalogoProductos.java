package PlataformasVentas;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Catálogo de la plataforma de ventas masivas.
 *
 * Son cuatro índices sobre los MISMOS objetos Producto (no hay copias):
 *   - porCodigo:    HashMap<String, Producto>        -> buscar por código, get O(1)
 *   - recientes:    ArrayDeque<Producto>             -> insertar al inicio de la lista, addFirst O(1)
 *   - porPrecio:    TreeSet<Producto>                -> productos ya ordenados por precio, insertar O(log n)
 *   - porCategoria: HashMap<String, HashSet<Producto>> -> filtrar por categoría O(1)
 */
public class CatalogoProductos {

    /**
     * Orden por precio, si no, por código.
     */
    public static final Comparator<Producto> POR_PRECIO =
            Comparator.comparingDouble(Producto::getPrecio).thenComparing(Producto::getCodigo);

    private final Map<String, Producto> porCodigo = new HashMap<>();
    private final Deque<Producto> recientes = new ArrayDeque<>();
    private final TreeSet<Producto> porPrecio = new TreeSet<>(POR_PRECIO);
    private final Map<String, Set<Producto>> porCategoria = new HashMap<>();

    /**
     * Inserta un producto nuevo al inicio de la lista y en los demás índices.
     */
    public boolean agregar(Producto producto) {
        if (porCodigo.putIfAbsent(producto.getCodigo(), producto) != null) {
            return false;
        }
        recientes.addFirst(producto);
        porPrecio.add(producto);
        porCategoria.computeIfAbsent(producto.getCategoria(), c -> new HashSet<>()).add(producto);
        return true;
    }

    public Producto buscarPorCodigo(String codigo) {
        return porCodigo.get(codigo);
    }

    /** Productos de una categoría (vacío si la categoría no existe). Sin orden. */
    public Set<Producto> filtrarPorCategoria(String categoria) {
        Set<Producto> productos = porCategoria.get(categoria);
        return productos == null ? Collections.emptySet() : Collections.unmodifiableSet(productos);
    }

    /** Vista de solo lectura, de menor a mayor precio. */
    public SortedSet<Producto> productosPorPrecio() {
        return Collections.unmodifiableSortedSet(porPrecio);
    }

    /** Vista de solo lectura, del producto más reciente al más antiguo. */
    public Collection<Producto> productosRecientes() {
        return Collections.unmodifiableCollection(recientes);
    }

    public int total() {
        return porCodigo.size();
    }
}