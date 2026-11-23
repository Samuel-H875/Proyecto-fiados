package fiados.modelo;

import java.time.LocalDate;

public class Movimiento {

    private final LocalDate fecha;
    private final TipoMovimiento tipo;
    private final long valor;
    private final String detalle;

    public Movimiento(LocalDate fecha, TipoMovimiento tipo, long valor, String detalle) {
        if (valor <= 0) {
            throw new IllegalArgumentException("El valor del movimiento debe ser mayor que cero.");
        }
        this.fecha = fecha;
        this.tipo = tipo;
        this.valor = valor;
        this.detalle = detalle == null ? "" : detalle.trim();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public long getValor() {
        return valor;
    }

    public String getDetalle() {
        return detalle;
    }
}
