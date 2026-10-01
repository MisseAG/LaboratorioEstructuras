package ecommerce;

public class Producto {
    private String codigo;
    private double precio;
    private String categoria;
    private int stock;

    public Producto(String codigo, double precio, String categoria, int stock) {
        this.codigo = codigo;
        this.precio = precio;
        this.categoria = categoria;
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
    public String toString() {
        return "Producto [Código=" + codigo + ", Precio=" + precio + ", Categoría=" + categoria + ", Stock=" + stock + "]";
    }
}