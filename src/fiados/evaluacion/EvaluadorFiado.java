package fiados.evaluacion;

import fiados.modelo.Cliente;
import fiados.modelo.Dinero;
import fiados.modelo.Movimiento;
import fiados.modelo.TipoMovimiento;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public abstract class EvaluadorFiado {

    public final ResultadoEvaluacion evaluar(Cliente cliente, long montoCompra, LocalDate hoy) {
        long saldo = consultarSaldo(cliente);
        List<Movimiento> historial = revisarHistorial(cliente);
        long limite = calcularLimite(cliente, historial);
        Optional<String> bloqueo = verificarCondiciones(historial, saldo, hoy);
        return decidir(saldo, montoCompra, limite, bloqueo);
    }

    public abstract String getTipo();

    public abstract String getDescripcionRegla();

    protected abstract long calcularLimite(Cliente cliente, List<Movimiento> historial);

    protected Optional<String> verificarCondiciones(List<Movimiento> historial, long saldo, LocalDate hoy) {
        return Optional.empty();
    }

    private long consultarSaldo(Cliente cliente) {
        return cliente.getSaldo();
    }

    private List<Movimiento> revisarHistorial(Cliente cliente) {
        return cliente.getMovimientos();
    }

    private ResultadoEvaluacion decidir(long saldo, long montoCompra, long limite, Optional<String> bloqueo) {
        long saldoNuevo = saldo + montoCompra;
        if (bloqueo.isPresent()) {
            return ResultadoEvaluacion.rechazado(bloqueo.get(), limite, saldo, saldoNuevo);
        }
        if (saldoNuevo > limite) {
            String motivo = "El saldo quedaría en " + Dinero.formatear(saldoNuevo)
                    + " y el límite para clientes de tipo '" + getTipo() + "' es " + Dinero.formatear(limite) + ".";
            return ResultadoEvaluacion.rechazado(motivo, limite, saldo, saldoNuevo);
        }
        return ResultadoEvaluacion.aprobado(limite, saldo, saldoNuevo);
    }

    protected static long totalAbonado(List<Movimiento> historial) {
        long total = 0;
        for (Movimiento m : historial) {
            if (m.getTipo() == TipoMovimiento.ABONO) {
                total += m.getValor();
            }
        }
        return total;
    }

    protected static int comprasSeguidasSinAbono(List<Movimiento> historial) {
        int cuenta = 0;
        for (Movimiento m : historial) {
            cuenta = (m.getTipo() == TipoMovimiento.ABONO) ? 0 : cuenta + 1;
        }
        return cuenta;
    }

    protected static long diasSinAbonar(List<Movimiento> historial, LocalDate hoy) {
        long saldo = 0;
        LocalDate referencia = null;
        for (Movimiento m : historial) {
            if (m.getTipo() == TipoMovimiento.COMPRA_FIADA) {
                if (saldo == 0) {
                    referencia = m.getFecha();
                }
                saldo += m.getValor();
            } else {
                saldo -= m.getValor();
                referencia = m.getFecha();
            }
        }
        if (saldo <= 0 || referencia == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(referencia, hoy);
    }
}
