package PlataformasVentas;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class MainProductos {

    private static final long CODIGO_BASE = 1_000_000_000L;
    private static final long SEMILLA = 42L;
    private static final int[] TAMANOS = {100, 1_000, 10_000, 100_000, 1_000_000};
    private static final String[] CATEGORIAS = new String[20];

    static {
        for (int i = 0; i < CATEGORIAS.length; i++) {
            CATEGORIAS[i] = "Categoria-" + i;
        }
    }

    private static class Resultado {
        int n;
        double msInsertar, msDuplicados, msBusqueda, msOrdenado, msFiltro;
        int duplicados, rechazados, consultas, recorridosFiltro;
        boolean verificacionOk;
        long bytesDatos, bytesEstructura;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            medir(20_000);
        }

        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("=== Catálogo de productos: pruebas ===");
            System.out.println(" 1) 100   2) 1.000   3) 10.000   4) 100.000   5) 1.000.000");
            System.out.println(" 6) Todas (con tabla resumen)");
            System.out.println(" 0) Salir");
            System.out.print("Elige una opción: ");
            if (!sc.hasNextLine()) break;
            String opcion = sc.nextLine().trim();
            try {
                switch (opcion) {
                    case "1": imprimir(medir(TAMANOS[0])); break;
                    case "2": imprimir(medir(TAMANOS[1])); break;
                    case "3": imprimir(medir(TAMANOS[2])); break;
                    case "4": imprimir(medir(TAMANOS[3])); break;
                    case "5": imprimir(medir(TAMANOS[4])); break;
                    case "6": todas(); break;
                    case "0": salir = true; break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (OutOfMemoryError e) {
                System.out.println("Sin memoria. Aumenta el heap, por ejemplo: -Xmx3g");
            }
        }
    }

    private static Resultado medir(int n) {
        Resultado r = new Resultado();
        r.n = n;
        Random rnd = new Random(SEMILLA);

        // Memoria de los datos (productos)
        long antes = memoriaUsada();
        Producto[] datos = new Producto[n];
        for (int i = 0; i < n; i++) {
            double precio = (100 + rnd.nextInt(1_000_000)) / 100.0; // 1,00 a 10.000,99
            datos[i] = new Producto("P" + (CODIGO_BASE + i), precio,
                    CATEGORIAS[rnd.nextInt(CATEGORIAS.length)], rnd.nextInt(500));
        }
        r.bytesDatos = Math.max(0, memoriaUsada() - antes);

        // Preparación fuera del cronómetro
        int k = Math.max(1, n / 10);
        Producto[] repetidos = new Producto[k];
        for (int i = 0; i < k; i++) {
            repetidos[i] = new Producto(datos[i].getCodigo(), 1.0, CATEGORIAS[0], 0);
        }
        int q = Math.min(Math.max(n, 10_000), 1_000_000);
        String[] claves = new String[q];
        for (int i = 0; i < q; i++) {
            claves[i] = datos[rnd.nextInt(n)].getCodigo();
        }

        // 1) Insertar n productos al inicio de la lista (actualiza los 4 índices) + memoria
        long base = memoriaUsada();
        CatalogoProductos catalogo = new CatalogoProductos();
        int insertados = 0;
        long t0 = System.nanoTime();
        for (Producto p : datos) {
            if (catalogo.agregar(p)) insertados++;
        }
        r.msInsertar = (System.nanoTime() - t0) / 1_000_000.0;
        r.bytesEstructura = Math.max(0, memoriaUsada() - base);

        // 2) Duplicados: todos deben ser rechazados
        int rechazados = 0;
        t0 = System.nanoTime();
        for (Producto p : repetidos) {
            if (!catalogo.agregar(p)) rechazados++;
        }
        r.msDuplicados = (System.nanoTime() - t0) / 1_000_000.0;
        r.duplicados = k;
        r.rechazados = rechazados;

        // 3) Buscar por código
        int aciertos = 0;
        t0 = System.nanoTime();
        for (String clave : claves) {
            if (catalogo.buscarPorCodigo(clave) != null) aciertos++;
        }
        r.msBusqueda = (System.nanoTime() - t0) / 1_000_000.0;
        r.consultas = q;

        // 4) Mostrar ordenado por precio (recorrer todo el TreeSet)
        int ordenados = 0;
        double sumaPrecios = 0;
        t0 = System.nanoTime();
        for (Producto p : catalogo.productosPorPrecio()) {
            ordenados++;
            sumaPrecios += p.getPrecio();
        }
        r.msOrdenado = (System.nanoTime() - t0) / 1_000_000.0;

        // 5) Filtrar por categoría (las 20 categorías; en total se recorren los n productos)
        int recorridos = 0;
        long sumaStock = 0;
        t0 = System.nanoTime();
        for (String categoria : CATEGORIAS) {
            Set<Producto> productos = catalogo.filtrarPorCategoria(categoria);
            for (Producto p : productos) {
                recorridos++;
                sumaStock += p.getStock();
            }
        }
        r.msFiltro = (System.nanoTime() - t0) / 1_000_000.0;
        r.recorridosFiltro = recorridos;

        // Verificaciones (no se cronometran)
        boolean ok = insertados == n && rechazados == k && aciertos == q
                && catalogo.total() == n && ordenados == n && recorridos == n
                && !Double.isNaN(sumaPrecios) && sumaStock >= 0;

        // Orden por precio estrictamente creciente (con desempate por código; no se perdió ninguno)
        Producto anterior = null;
        for (Producto p : catalogo.productosPorPrecio()) {
            if (anterior != null && CatalogoProductos.POR_PRECIO.compare(anterior, p) >= 0) {
                ok = false;
                break;
            }
            anterior = p;
        }

        // Inserción al inicio: el último insertado debe quedar primero
        int i = n - 1;
        for (Producto p : catalogo.productosRecientes()) {
            if (p != datos[i--]) {
                ok = false;
                break;
            }
        }
        r.verificacionOk = ok;
        return r;
    }

    private static void todas() {
        List<Resultado> lista = new ArrayList<>();
        for (int n : TAMANOS) {
            Resultado r = medir(n);
            imprimir(r);
            lista.add(r);
        }
        System.out.println();
        System.out.println("=== Resumen ===");
        System.out.printf("%-10s %-13s %-15s %-14s %-12s %-14s %-13s %-12s %-12s %-12s%n",
                "Productos", "Insertar (ms)", "Duplicados (ms)", "Búsqueda (ms)", "ns/consulta",
                "Ordenado (ms)", "Filtro (ms)", "Estructura", "Datos", "Total");
        for (Resultado r : lista) {
            System.out.printf("%-,10d %-13.3f %-15.3f %-14.3f %-12.0f %-14.3f %-13.3f %-12s %-12s %-12s%n",
                    r.n, r.msInsertar, r.msDuplicados, r.msBusqueda,
                    r.msBusqueda * 1_000_000 / r.consultas, r.msOrdenado, r.msFiltro,
                    formato(r.bytesEstructura), formato(r.bytesDatos),
                    formato(r.bytesEstructura + r.bytesDatos));
        }
    }

    private static void imprimir(Resultado r) {
        System.out.println();
        System.out.printf("=== %,d productos ===%n", r.n);
        System.out.printf("Insertar al inicio: %,d productos (4 índices) en %.3f ms%n", r.n, r.msInsertar);
        System.out.printf("Duplicados:         %,d/%,d rechazados en %.3f ms%n", r.rechazados, r.duplicados, r.msDuplicados);
        System.out.printf("Buscar por código:  %,d consultas en %.3f ms  (%.0f ns por consulta)%n",
                r.consultas, r.msBusqueda, r.msBusqueda * 1_000_000 / r.consultas);
        System.out.printf("Recorrer por precio: %,d productos en %.3f ms%n", r.n, r.msOrdenado);
        System.out.printf("Filtrar categorías:  %,d productos recorridos (20 categorías) en %.3f ms%n",
                r.recorridosFiltro, r.msFiltro);
        System.out.printf("Verificación (orden por precio, inicio de lista, sin pérdidas): %s%n",
                r.verificacionOk ? "OK" : "FALLA");
        System.out.printf("Memoria:            estructura %s | datos %s | total %s%n",
                formato(r.bytesEstructura), formato(r.bytesDatos),
                formato(r.bytesEstructura + r.bytesDatos));
    }

    /** Memoria usada tras pedir varios GC para que la diferencia sea más estable. */
    private static long memoriaUsada() {
        Runtime rt = Runtime.getRuntime();
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return rt.totalMemory() - rt.freeMemory();
    }

    private static String formato(long bytes) {
        if (bytes < 1024 * 1024) return String.format("%.2f kB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}