package fiados.evaluacion;

import fiados.modelo.Dinero;

public class ResultadoEvaluacion {

    private final boolean aprobado;
    private final String motivo;
    private final long limite;
    private final long saldoActual;
    private final long saldoNuevo;

    private ResultadoEvaluacion(boolean aprobado, String motivo, long limite, long saldoActual, long saldoNuevo) {
        this.aprobado = aprobado;
        this.motivo = motivo;
        this.limite = limite;
        this.saldoActual = saldoActual;
        this.saldoNuevo = saldoNuevo;
    }

    public static ResultadoEvaluacion aprobado(long limite, long saldoActual, long saldoNuevo) {
        String motivo = "Dentro del límite de " + Dinero.formatear(limite) + ".";
        return new ResultadoEvaluacion(true, motivo, limite, saldoActual, saldoNuevo);
    }

    public static ResultadoEvaluacion rechazado(String motivo, long limite, long saldoActual, long saldoNuevo) {
        return new ResultadoEvaluacion(false, motivo, limite, saldoActual, saldoNuevo);
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public String getMotivo() {
        return motivo;
    }

    public long getLimite() {
        return limite;
    }

    public long getSaldoActual() {
        return saldoActual;
    }

    public long getSaldoNuevo() {
        return saldoNuevo;
    }
}
