package ecommerce;

import java.util.*;

public class CatalogoProductos {
    private HashMap<String, Producto> mapaPorCodigo;
    private TreeSet<Producto> setPorPrecio;
    private LinkedHashSet<Producto> setRecientes;
    private HashMap<String, Set<Producto>> mapaPorCategoria;

    public static final Comparator<Producto> POR_PRECIO = (p1, p2) -> {
        int comp = Double.compare(p1.getPrecio(), p2.getPrecio());
        if (comp == 0) {
            return p1.getCodigo().compareTo(p2.getCodigo());
        }
        return comp;
    };

    public CatalogoProductos() {
        this.mapaPorCodigo = new HashMap<>();
        this.setPorPrecio = new TreeSet<>(POR_PRECIO);
        this.setRecientes = new LinkedHashSet<>();
        this.mapaPorCategoria = new HashMap<>();
    }

    public boolean agregar(Producto p) {
        if (mapaPorCodigo.containsKey(p.getCodigo())) {
            return false; // Duplicado rechazado
        }

        mapaPorCodigo.put(p.getCodigo(), p);
        setPorPrecio.add(p);
        setRecientes.add(p);

        mapaPorCategoria.computeIfAbsent(p.getCategoria(), k -> new HashSet<>()).add(p);
        return true;
    }

    public Producto buscarPorCodigo(String codigo) {
        return mapaPorCodigo.get(codigo);
    }

    public SortedSet<Producto> productosPorPrecio() {
        return setPorPrecio;
    }

    public Set<Producto> filtrarPorCategoria(String categoria) {
        return mapaPorCategoria.getOrDefault(categoria, Collections.emptySet());
    }

    public int total() {
        return mapaPorCodigo.size();
    }

    public LinkedHashSet<Producto> productosRecientes() {
        return setRecientes;
    }
}