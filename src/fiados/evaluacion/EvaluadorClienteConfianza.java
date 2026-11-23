package fiados.evaluacion;

import fiados.modelo.Cliente;
import fiados.modelo.Dinero;
import fiados.modelo.Movimiento;

public class EvaluadorClienteConfianza extends EvaluadorFiado {

    private static final long LIMITE_BASE = 300_000;
    private static final long LIMITE_MAXIMO = 600_000;
    private static final int PORCENTAJE_DE_LO_ABONADO = 10;

    @Override
    public String getTipo() {
        return "confianza";
    }

    @Override
    public String getDescripcionRegla() {
        return "límite " + Dinero.formatear(LIMITE_BASE) + " + " + PORCENTAJE_DE_LO_ABONADO
                + "% de lo abonado, tope " + Dinero.formatear(LIMITE_MAXIMO);
    }

    @Override
    protected long calcularLimite(Cliente cliente, java.util.List<Movimiento> historial) {
        long limite = LIMITE_BASE + totalAbonado(historial) * PORCENTAJE_DE_LO_ABONADO / 100;
        return Math.min(limite, LIMITE_MAXIMO);
    }
}
