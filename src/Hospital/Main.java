package Hospital;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Fase 4: pruebas del registro de pacientes (tiempo y memoria medidos desde el código).
 *
 * Tiempo:  System.nanoTime() alrededor de cada operación.
 * Memoria: (totalMemory - freeMemory) después de forzar el GC, en dos partes:
 *            - datos:      los objetos Paciente (+ arreglo que los guarda)
 *            - estructura: lo que añade el LinkedHashMap encima de los pacientes
 *          Es aproximada: System.gc() es una sugerencia para la JVM, no una orden.
 */
public class Main {

    private static final long DOCUMENTO_BASE = 1_000_000_000L;
    private static final long SEMILLA = 42L;
    private static final int[] TAMANOS = {100, 1_000, 10_000, 100_000, 1_000_000};

    private static class Resultado {
        int n;
        double msRegistro, msDuplicados, msBusqueda;
        int duplicados, rechazados, consultas;
        long bytesDatos, bytesEstructura;
    }

    public static void main(String[] args) {
        // Calentamiento: que el JIT compile el código antes de medir (no imprime nada)
        for (int i = 0; i < 3; i++) {
            medir(20_000);
        }

        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("=== Registro de pacientes: pruebas ===");
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
                System.out.println("Sin memoria. Aumenta el heap, por ejemplo: -Xmx2g");
            }
        }
    }

    private static Resultado medir(int n) {
        Resultado r = new Resultado();
        r.n = n;

        // Memoria de los datos (pacientes)
        long antes = memoriaUsada();
        Paciente[] datos = new Paciente[n];
        for (int i = 0; i < n; i++) {
            datos[i] = new Paciente(String.valueOf(DOCUMENTO_BASE + i), "Paciente " + i);
        }
        r.bytesDatos = Math.max(0, memoriaUsada() - antes);

        // Preparación fuera del cronómetro
        int k = Math.max(1, n / 10);
        Paciente[] repetidos = new Paciente[k];
        for (int i = 0; i < k; i++) {
            repetidos[i] = new Paciente(datos[i].getDocumento(), "Duplicado " + i);
        }
        int q = Math.min(Math.max(n, 10_000), 1_000_000);
        Random rnd = new Random(SEMILLA);
        String[] claves = new String[q];
        for (int i = 0; i < q; i++) {
            claves[i] = datos[rnd.nextInt(n)].getDocumento();
        }

        // Registro + memoria de la estructura
        long base = memoriaUsada();
        RegistroPacientes registro = new RegistroPacientes();
        long t0 = System.nanoTime();
        for (Paciente p : datos) {
            registro.registrar(p);
        }
        r.msRegistro = (System.nanoTime() - t0) / 1_000_000.0;
        r.bytesEstructura = Math.max(0, memoriaUsada() - base);

        // Duplicados
        int rechazados = 0;
        t0 = System.nanoTime();
        for (Paciente p : repetidos) {
            if (!registro.registrar(p)) rechazados++;
        }
        r.msDuplicados = (System.nanoTime() - t0) / 1_000_000.0;
        r.duplicados = k;
        r.rechazados = rechazados;

        // Búsqueda
        int aciertos = 0;
        t0 = System.nanoTime();
        for (String clave : claves) {
            if (registro.buscarPorDocumento(clave) != null) aciertos++;
        }
        r.msBusqueda = (System.nanoTime() - t0) / 1_000_000.0;
        r.consultas = q;

        // Evita que la JVM descarte código o datos antes de tiempo
        if (aciertos != q || registro.total() != datos.length) {
            throw new IllegalStateException("Resultado inesperado");
        }
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
        System.out.printf("%-10s %-14s %-16s %-16s %-14s %-12s %-12s %-12s%n",
                "Pacientes", "Registro (ms)", "Duplicados (ms)", "Búsqueda (ms)",
                "ns/consulta", "Estructura", "Datos", "Total");
        for (Resultado r : lista) {
            System.out.printf("%-,10d %-14.3f %-16.3f %-16.3f %-14.0f %-12s %-12s %-12s%n",
                    r.n, r.msRegistro, r.msDuplicados, r.msBusqueda,
                    r.msBusqueda * 1_000_000 / r.consultas,
                    formato(r.bytesEstructura), formato(r.bytesDatos),
                    formato(r.bytesEstructura + r.bytesDatos));
        }
    }

    private static void imprimir(Resultado r) {
        System.out.println();
        System.out.printf("=== %,d pacientes ===%n", r.n);
        System.out.printf("Registro:   %,d registrados en %.3f ms%n", r.n, r.msRegistro);
        System.out.printf("Duplicados: %,d/%,d rechazados en %.3f ms%n", r.rechazados, r.duplicados, r.msDuplicados);
        System.out.printf("Búsqueda:   %,d consultas en %.3f ms  (%.0f ns por consulta)%n",
                r.consultas, r.msBusqueda, r.msBusqueda * 1_000_000 / r.consultas);
        System.out.printf("Memoria:    estructura %s | datos %s | total %s%n",
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