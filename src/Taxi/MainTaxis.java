package Taxi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class MainTaxis {

    private static final long ID_BASE = 1_000_000_000L;
    private static final long SEMILLA = 42L;
    private static final int[] TAMANOS = {100, 1_000, 10_000, 100_000, 1_000_000};
    private static final String[] LUGARES = new String[30];

    static {
        for (int i = 0; i < LUGARES.length; i++) {
            LUGARES[i] = "Lugar-" + i;
        }
    }

    private static class Resultado {
        int n, duplicados, rechazados, cancelaciones, atenciones, pendientesFinal;
        double msRegistrar, msDuplicados, msCancelar, msAtender, msMostrar;
        boolean verificacionOk;
        long bytesDatos, bytesEstructura;
    }

    static void main() {
        for (int i = 0; i < 3; i++) {
            medir(20_000);
        }

        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("=== Plataforma de taxis: pruebas ===");
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
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);

        // Memoria de los datos (solicitudes). La hora crece con i: el orden de llegada es el de la hora.
        long antes = memoriaUsada();
        SolicitudDeViaje[] datos = new SolicitudDeViaje[n];
        for (int i = 0; i < n; i++) {
            datos[i] = new SolicitudDeViaje("S" + (ID_BASE + i), inicio.plusSeconds(i),
                    LUGARES[rnd.nextInt(LUGARES.length)]);
        }
        r.bytesDatos = Math.max(0, memoriaUsada() - antes);

        // Preparación fuera del cronómetro
        int k = Math.max(1, n / 10);
        SolicitudDeViaje[] repetidas = new SolicitudDeViaje[k];
        for (int i = 0; i < k; i++) {
            repetidas[i] = new SolicitudDeViaje(datos[i].getId(), inicio, "Duplicada");
        }

        int c = Math.max(1, n / 10);              // cancelaciones
        int[] indices = new int[n];               // se barajan los índices para elegir c al azar
        for (int i = 0; i < n; i++) indices[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = indices[i];
            indices[i] = indices[j];
            indices[j] = tmp;
        }
        boolean[] cancelada = new boolean[n];
        String[] idsCancelar = new String[c];
        for (int i = 0; i < c; i++) {
            cancelada[indices[i]] = true;
            idsCancelar[i] = datos[indices[i]].getId();
        }
        int m = Math.max(1, (n - c) / 2);         // atenciones
        SolicitudDeViaje[] atendidas = new SolicitudDeViaje[m];

        // 1) Registrar n solicitudes + memoria de la estructura
        long base = memoriaUsada();
        PlataformaTaxis plataforma = new PlataformaTaxis();
        int registradas = 0;
        long t0 = System.nanoTime();
        for (SolicitudDeViaje s : datos) {
            if (plataforma.registrar(s)) registradas++;
        }
        r.msRegistrar = (System.nanoTime() - t0) / 1_000_000.0;
        r.bytesEstructura = Math.max(0, memoriaUsada() - base);

        // 2) Duplicados: todos deben ser rechazados
        int rechazados = 0;
        t0 = System.nanoTime();
        for (SolicitudDeViaje s : repetidas) {
            if (!plataforma.registrar(s)) rechazados++;
        }
        r.msDuplicados = (System.nanoTime() - t0) / 1_000_000.0;
        r.duplicados = k;
        r.rechazados = rechazados;

        // 3) Cancelar c solicitudes específicas por id
        int canceladas = 0;
        t0 = System.nanoTime();
        for (String id : idsCancelar) {
            if (plataforma.cancelar(id)) canceladas++;
        }
        r.msCancelar = (System.nanoTime() - t0) / 1_000_000.0;
        r.cancelaciones = c;

        // 4) Atender las m solicitudes más antiguas
        t0 = System.nanoTime();
        for (int i = 0; i < m; i++) {
            atendidas[i] = plataforma.atenderSiguiente();
        }
        r.msAtender = (System.nanoTime() - t0) / 1_000_000.0;
        r.atenciones = m;

        // 5) Mostrar las pendientes que quedan (recorrerlas en orden)
        int mostradas = 0;
        long sumaSegundos = 0;
        t0 = System.nanoTime();
        for (SolicitudDeViaje s : plataforma.pendientes()) {
            mostradas++;
            sumaSegundos += s.getHora().getSecond();
        }
        r.msMostrar = (System.nanoTime() - t0) / 1_000_000.0;
        r.pendientesFinal = mostradas;

        // Verificaciones (no se cronometran)
        boolean ok = registradas == n && rechazados == k && canceladas == c
                && mostradas == n - c - m && plataforma.totalPendientes() == mostradas
                && sumaSegundos >= 0;

        // Las atendidas deben ser las más antiguas NO canceladas, en orden de llegada
        int idx = 0;
        for (int j = 0; j < m; j++) {
            while (idx < n && cancelada[idx]) idx++;
            if (idx >= n || atendidas[j] != datos[idx]
                    || atendidas[j].getEstado() != SolicitudDeViaje.Estado.ATENDIDA) {
                ok = false;
                break;
            }
            idx++;
        }
        // Las pendientes deben ser exactamente el resto, en orden de llegada
        for (SolicitudDeViaje s : plataforma.pendientes()) {
            while (idx < n && cancelada[idx]) idx++;
            if (idx >= n || s != datos[idx]) {
                ok = false;
                break;
            }
            idx++;
        }
        while (idx < n && cancelada[idx]) idx++;
        if (idx != n) ok = false;

        // Cancelar algo ya cancelado o ya atendido debe devolver false
        if (plataforma.cancelar(idsCancelar[0]) || plataforma.cancelar(atendidas[0].getId())) ok = false;

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
        System.out.printf("%-10s %-13s %-15s %-13s %-12s %-12s %-12s %-13s %-12s %-12s %-12s%n",
                "Solicitud.", "Registrar(ms)", "Duplicados(ms)", "Cancelar(ms)", "ns/cancelar",
                "Atender(ms)", "ns/atender", "Mostrar(ms)", "Estructura", "Datos", "Total");
        for (Resultado r : lista) {
            System.out.printf("%-,10d %-13.3f %-15.3f %-13.3f %-12.0f %-12.3f %-12.0f %-13.3f %-12s %-12s %-12s%n",
                    r.n, r.msRegistrar, r.msDuplicados, r.msCancelar,
                    r.msCancelar * 1_000_000 / r.cancelaciones,
                    r.msAtender, r.msAtender * 1_000_000 / r.atenciones, r.msMostrar,
                    formato(r.bytesEstructura), formato(r.bytesDatos),
                    formato(r.bytesEstructura + r.bytesDatos));
        }
    }

    private static void imprimir(Resultado r) {
        System.out.println();
        System.out.printf("=== %,d solicitudes ===%n", r.n);
        System.out.printf("Registrar:  %,d solicitudes en %.3f ms%n", r.n, r.msRegistrar);
        System.out.printf("Duplicados: %,d/%,d rechazadas en %.3f ms%n", r.rechazados, r.duplicados, r.msDuplicados);
        System.out.printf("Cancelar:   %,d solicitudes por id en %.3f ms  (%.0f ns por cancelación)%n",
                r.cancelaciones, r.msCancelar, r.msCancelar * 1_000_000 / r.cancelaciones);
        System.out.printf("Atender:    %,d solicitudes (las más antiguas) en %.3f ms  (%.0f ns por atención)%n",
                r.atenciones, r.msAtender, r.msAtender * 1_000_000 / r.atenciones);
        System.out.printf("Mostrar:    %,d pendientes recorridas en %.3f ms%n", r.pendientesFinal, r.msMostrar);
        System.out.printf("Verificación (orden de atención, cancelaciones, pendientes): %s%n",
                r.verificacionOk ? "OK" : "FALLA");
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