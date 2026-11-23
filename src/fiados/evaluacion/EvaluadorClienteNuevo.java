package fiados.evaluacion;

import fiados.modelo.Cliente;
import fiados.modelo.Dinero;
import fiados.modelo.Movimiento;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EvaluadorClienteNuevo extends EvaluadorFiado {

    private static final long LIMITE = 50_000;
    private static final int MAX_COMPRAS_SEGUIDAS = 2;

    @Override
    public String getTipo() {
        return "nuevo";
    }

    @Override
    public String getDescripcionRegla() {
        return "límite " + Dinero.formatear(LIMITE) + "; máximo " + MAX_COMPRAS_SEGUIDAS
                + " compras fiadas seguidas sin abonar";
    }

    @Override
    protected long calcularLimite(Cliente cliente, List<Movimiento> historial) {
        return LIMITE;
    }

    @Override
    protected Optional<String> verificarCondiciones(List<Movimiento> historial, long saldo, LocalDate hoy) {
        if (comprasSeguidasSinAbono(historial) >= MAX_COMPRAS_SEGUIDAS) {
            return Optional.of("Cliente nuevo con " + MAX_COMPRAS_SEGUIDAS
                    + " compras fiadas seguidas sin abonar. Debe abonar antes de fiarle de nuevo.");
        }
        return Optional.empty();
    }
}
