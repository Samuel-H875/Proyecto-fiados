package fiados.evaluacion;

import fiados.modelo.Cliente;
import fiados.modelo.Dinero;
import fiados.modelo.Movimiento;
import java.util.List;
import java.util.Optional;

public class EvaluadorClienteConocido extends EvaluadorFiado {

    private static final long LIMITE = 150_000;
    private static final int DIAS_MAX_SIN_ABONAR = 30;

    @Override
    public String getTipo() {
        return "conocido";
    }

    @Override
    public String getDescripcionRegla() {
        return "límite " + Dinero.formatear(LIMITE) + "; se bloquea si pasan más de "
                + DIAS_MAX_SIN_ABONAR + " días sin abonar";
    }

    @Override
    protected long calcularLimite(Cliente cliente, List<Movimiento> historial) {
        return LIMITE;
    }

    @Override
    protected Optional<String> verificarCondiciones(List<Movimiento> historial, long saldo, java.time.LocalDate hoy) {
        long dias = diasSinAbonar(historial, hoy);
        if (dias > DIAS_MAX_SIN_ABONAR) {
            return Optional.of("Cliente en mora: lleva " + dias + " días sin abonar (máximo "
                    + DIAS_MAX_SIN_ABONAR + ").");
        }
        return Optional.empty();
    }
}
