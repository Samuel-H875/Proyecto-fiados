package fiados.evaluacion;

import fiados.modelo.Cliente;
import fiados.modelo.Dinero;
import fiados.modelo.Movimiento;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EvaluadorClienteQuincenal extends EvaluadorFiado {

    private static final long LIMITE = 200_000;
    private static final int DIAS_MAX_SIN_ABONAR = 15;

    @Override
    public String getTipo() {
        return "quincenal";
    }

    @Override
    public String getDescripcionRegla() {
        return "límite " + Dinero.formatear(LIMITE) + "; se bloquea si pasa más de una quincena ("
                + DIAS_MAX_SIN_ABONAR + " días) sin abonar";
    }

    @Override
    protected long calcularLimite(Cliente cliente, List<Movimiento> historial) {
        return LIMITE;
    }

    @Override
    protected Optional<String> verificarCondiciones(List<Movimiento> historial, long saldo, LocalDate hoy) {
        long dias = diasSinAbonar(historial, hoy);
        if (dias > DIAS_MAX_SIN_ABONAR) {
            return Optional.of("Pasó más de una quincena sin abonar: " + dias + " días (máximo "
                    + DIAS_MAX_SIN_ABONAR + ").");
        }
        return Optional.empty();
    }
}
