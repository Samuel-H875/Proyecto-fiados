package pruebas;

import fiados.evaluacion.EvaluadorClienteConfianza;
import fiados.evaluacion.EvaluadorClienteConocido;
import fiados.evaluacion.EvaluadorClienteNuevo;
import fiados.evaluacion.EvaluadorClienteQuincenal;
import fiados.evaluacion.EvaluadorFiado;
import fiados.evaluacion.RegistroEvaluadores;
import fiados.evaluacion.ResultadoEvaluacion;
import fiados.modelo.Cliente;
import fiados.modelo.Movimiento;
import fiados.modelo.TipoMovimiento;
import fiados.servicio.RepositorioClientes;
import fiados.servicio.ServicioFiados;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class PruebaFlujo {

    private static int pruebas = 0;
    private static int fallos = 0;

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"), ZoneId.of("UTC"));
    private static final LocalDate HOY = LocalDate.now(RELOJ);

    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        pruebaClienteNuevo();
        pruebaClienteConocido();
        pruebaClienteConfianza();
        pruebaAbonos();
        pruebaExtensionQuincenal();
        pruebaPlantillaNoSobrescribible();
        System.out.println();
        System.out.println(pruebas + " verificaciones, " + fallos + " fallos.");
        if (fallos > 0) {
            System.exit(1);
        }
    }

    private static ServicioFiados nuevoServicio() {
        RegistroEvaluadores registro = new RegistroEvaluadores();
        registro.registrar(new EvaluadorClienteNuevo());
        registro.registrar(new EvaluadorClienteConocido());
        registro.registrar(new EvaluadorClienteConfianza());
        registro.registrar(new EvaluadorClienteQuincenal());
        return new ServicioFiados(new RepositorioClientes(), registro, RELOJ);
    }

    private static void pruebaClienteNuevo() {
        System.out.println("-- Cliente nuevo");
        ServicioFiados s = nuevoServicio();
        Cliente c = s.registrarCliente("Nuevo 1", "nuevo");
        verificar("aprueba 30.000", s.registrarFiado(c.getId(), 30_000, "").isAprobado());
        verificar("rechaza si el saldo pasa de 50.000", !s.registrarFiado(c.getId(), 30_000, "").isAprobado());
        verificar("aprueba 20.000 (saldo 50.000 justo en el limite)",
                s.registrarFiado(c.getId(), 20_000, "").isAprobado());
        ResultadoEvaluacion r = s.registrarFiado(c.getId(), 1_000, "");
        verificar("rechaza la tercera compra seguida sin abonar",
                !r.isAprobado() && r.getMotivo().contains("seguidas"));
        s.registrarAbono(c.getId(), 10_000);
        verificar("tras abonar vuelve a poder fiar 5.000", s.registrarFiado(c.getId(), 5_000, "").isAprobado());
    }

    private static void pruebaClienteConocido() {
        System.out.println("-- Cliente conocido");
        ServicioFiados s = nuevoServicio();
        Cliente c = s.registrarCliente("Conocido 1", "conocido");
        verificar("aprueba 150.000 (justo el limite)", s.registrarFiado(c.getId(), 150_000, "").isAprobado());
        verificar("rechaza 1 peso por encima del limite", !s.registrarFiado(c.getId(), 1, "").isAprobado());

        Cliente mora = s.registrarCliente("Conocido en mora", "conocido");
        s.registrarMovimientoHistorico(mora.getId(),
                new Movimiento(HOY.minusDays(45), TipoMovimiento.COMPRA_FIADA, 40_000, ""));
        ResultadoEvaluacion r = s.registrarFiado(mora.getId(), 10_000, "");
        verificar("rechaza por mora (45 días sin abonar)", !r.isAprobado() && r.getMotivo().contains("mora"));
        s.registrarAbono(mora.getId(), 5_000);
        verificar("un abono reinicia el conteo y vuelve a aprobar",
                s.registrarFiado(mora.getId(), 10_000, "").isAprobado());

        Cliente alDia = s.registrarCliente("Pagó todo", "conocido");
        s.registrarMovimientoHistorico(alDia.getId(),
                new Movimiento(HOY.minusDays(90), TipoMovimiento.COMPRA_FIADA, 40_000, ""));
        s.registrarMovimientoHistorico(alDia.getId(),
                new Movimiento(HOY.minusDays(80), TipoMovimiento.ABONO, 40_000, ""));
        verificar("deuda saldada hace mucho no cuenta como mora",
                s.registrarFiado(alDia.getId(), 10_000, "").isAprobado());
    }

    private static void pruebaClienteConfianza() {
        System.out.println("-- Cliente de confianza");
        ServicioFiados s = nuevoServicio();
        Cliente c = s.registrarCliente("Confianza 1", "confianza");
        verificar("aprueba 300.000 (límite base)", s.registrarFiado(c.getId(), 300_000, "").isAprobado());
        verificar("rechaza 1 peso más sin abonos previos", !s.registrarFiado(c.getId(), 1, "").isAprobado());
        s.registrarAbono(c.getId(), 200_000); // límite sube a 300.000 + 20.000 = 320.000; saldo 100.000
        verificar("con 200.000 abonados el límite es 320.000: aprueba 220.000",
                s.registrarFiado(c.getId(), 220_000, "").isAprobado());
        verificar("pero no pasa de 320.000", !s.registrarFiado(c.getId(), 1, "").isAprobado());

        Cliente tope = s.registrarCliente("Confianza tope", "confianza");
        for (int i = 0; i < 4; i++) {
            s.registrarFiado(tope.getId(), 300_000, "");
            s.registrarAbono(tope.getId(), 300_000);
        }
        for (int i = 0; i < 20; i++) {
            s.registrarFiado(tope.getId(), 300_000, "");
            s.registrarAbono(tope.getId(), 300_000);
        }
        verificar("el límite no supera el tope de 600.000", s.registrarFiado(tope.getId(), 600_000, "").isAprobado()
                && !s.registrarFiado(tope.getId(), 1, "").isAprobado());
    }

    private static void pruebaAbonos() {
        System.out.println("-- Abonos y saldo");
        ServicioFiados s = nuevoServicio();
        Cliente c = s.registrarCliente("Abonos", "conocido");
        s.registrarFiado(c.getId(), 100_000, "");
        s.registrarAbono(c.getId(), 40_000);
        verificar("abono parcial deja saldo 60.000", c.getSaldo() == 60_000);
        boolean excede = false;
        try {
            s.registrarAbono(c.getId(), 70_000);
        } catch (IllegalArgumentException e) {
            excede = true;
        }
        verificar("rechaza un abono mayor al saldo", excede && c.getSaldo() == 60_000);
        s.registrarAbono(c.getId(), 60_000);
        verificar("abono total deja saldo 0", c.getSaldo() == 0);
        verificar("el estado de cuenta muestra el saldo pendiente",
                s.estadoDeCuenta(c.getId()).contains("SALDO PENDIENTE: $0"));
        boolean tipoInvalido = false;
        try {
            s.registrarCliente("X", "inexistente");
        } catch (IllegalArgumentException e) {
            tipoInvalido = true;
        }
        verificar("no permite registrar un tipo de cliente que no existe", tipoInvalido);
    }

    private static void pruebaExtensionQuincenal() {
        System.out.println("-- Prueba de extensión: tipo 'quincenal'");
        ServicioFiados s = nuevoServicio();
        verificar("el menú ofrece 4 tipos sin haber tocado el servicio", s.tiposDisponibles().size() == 4);
        Cliente c = s.registrarCliente("Quincenal 1", "quincenal");
        verificar("aprueba 200.000 (límite del tipo nuevo)", s.registrarFiado(c.getId(), 200_000, "").isAprobado());
        verificar("rechaza por encima de 200.000", !s.registrarFiado(c.getId(), 1, "").isAprobado());
        Cliente atrasado = s.registrarCliente("Quincenal atrasado", "quincenal");
        s.registrarMovimientoHistorico(atrasado.getId(),
                new Movimiento(HOY.minusDays(16), TipoMovimiento.COMPRA_FIADA, 50_000, ""));
        verificar("rechaza si pasó más de una quincena sin abonar",
                !s.registrarFiado(atrasado.getId(), 10_000, "").isAprobado());
    }

    private static void pruebaPlantillaNoSobrescribible() throws Exception {
        System.out.println("-- Estructura del patrón");
        Method plantilla = EvaluadorFiado.class.getMethod("evaluar", Cliente.class, long.class, LocalDate.class);
        verificar("evaluar() es final: ninguna subclase puede cambiar el orden de los pasos",
                Modifier.isFinal(plantilla.getModifiers()));
        verificar("EvaluadorFiado es abstracta", Modifier.isAbstract(EvaluadorFiado.class.getModifiers()));
    }

    private static void verificar(String descripcion, boolean condicion) {
        pruebas++;
        if (!condicion) {
            fallos++;
        }
        System.out.println((condicion ? "  [OK]    " : "  [FALLA] ") + descripcion);
    }
}
