package PlataformasVentas;
import java.util.Objects;

/**
 * Producto del catálogo. El código es el identificador único
 **/
public class Producto {

    private final String codigo;
    private final double precio;
    private final String categoria;
    private final int stock;

    public Producto(String codigo, double precio, String categoria, int stock) {
        this.codigo = Objects.requireNonNull(codigo, "El código es obligatorio");
        this.precio = precio;
        this.categoria = Objects.requireNonNull(categoria, "La categoría es obligatoria");
        this.stock = stock;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getPrecio() {
        return precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        return codigo.equals(((Producto) o).codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return "Producto{codigo=" + codigo + ", precio=" + precio
                + ", categoria=" + categoria + ", stock=" + stock + "}";
    }
}