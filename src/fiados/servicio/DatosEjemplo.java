package fiados.servicio;

import fiados.modelo.Cliente;
import fiados.modelo.Movimiento;
import fiados.modelo.TipoMovimiento;
import java.time.LocalDate;

public final class DatosEjemplo {

    private DatosEjemplo() {
    }

    public static void cargar(ServicioFiados servicio) {
        LocalDate hoy = servicio.hoy();

        Cliente ana = servicio.registrarCliente("Ana Pérez", "conocido");
        compra(servicio, ana, hoy.minusDays(45), 80_000, "arroz, aceite, panela");

        Cliente luis = servicio.registrarCliente("Luis Gómez", "nuevo");
        compra(servicio, luis, hoy.minusDays(3), 20_000, "huevos y leche");
        compra(servicio, luis, hoy.minusDays(1), 15_000, "pan y café");

        Cliente maria = servicio.registrarCliente("María Ruiz", "confianza");
        compra(servicio, maria, hoy.minusDays(20), 200_000, "mercado del mes");
        abono(servicio, maria, hoy.minusDays(10), 150_000);

        servicio.registrarCliente("Carlos Díaz", "conocido");
    }

    private static void compra(ServicioFiados s, Cliente c, LocalDate fecha, long valor, String detalle) {
        s.registrarMovimientoHistorico(c.getId(), new Movimiento(fecha, TipoMovimiento.COMPRA_FIADA, valor, detalle));
    }

    private static void abono(ServicioFiados s, Cliente c, LocalDate fecha, long valor) {
        s.registrarMovimientoHistorico(c.getId(), new Movimiento(fecha, TipoMovimiento.ABONO, valor, ""));
    }
}
