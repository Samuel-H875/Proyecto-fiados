package fiados.modelo;

public enum TipoCliente {
    NUEVO("nuevo"),
    CONOCIDO("conocido"),
    CONFIANZA("confianza"),
    QUINCENAL("quincenal");

    private final String clave;

    TipoCliente(String clave) {
        this.clave = clave;
    }

    public String getClave() {
        return clave;
    }
}