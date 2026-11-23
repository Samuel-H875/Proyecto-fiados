package fiados.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cliente {

    private final int id;
    private final String nombre;
    private String tipo;
    private final List<Movimiento> movimientos = new ArrayList<>();

    public Cliente(int id, String nombre, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void agregarMovimiento(Movimiento movimiento) {
        movimientos.add(movimiento);
    }

    public List<Movimiento> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    public long getSaldo() {
        long saldo = 0;
        for (Movimiento m : movimientos) {
            saldo += m.getTipo().aplicarASaldo(m.getValor());
        }
        return saldo;
    }
}