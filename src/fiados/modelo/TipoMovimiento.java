package fiados.modelo;

public enum TipoMovimiento {

    COMPRA_FIADA("Compra fiada", "La deuda del cliente sube", +1),
    ABONO("Abono", "La deuda del cliente baja", -1);

    private final String etiqueta;
    private final String descripcion;
    private final int signo;

    TipoMovimiento(String etiqueta, String descripcion, int signo) {
        this.etiqueta = etiqueta;
        this.descripcion = descripcion;
        this.signo = signo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getSigno() {
        return signo;
    }

    public long aplicarASaldo(long valor) {
        return signo * valor;
    }
}