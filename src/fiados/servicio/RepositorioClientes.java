package fiados.servicio;

import fiados.modelo.Cliente;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class RepositorioClientes {

    private final Map<Integer, Cliente> clientes = new TreeMap<>();
    private int ultimoId = 0;

    public int siguienteId() {
        return ++ultimoId;
    }

    public void guardar(Cliente cliente) {
        clientes.put(cliente.getId(), cliente);
    }

    public Optional<Cliente> buscarPorId(int id) {
        return Optional.ofNullable(clientes.get(id));
    }

    public List<Cliente> listar() {
        return new ArrayList<>(clientes.values());
    }
}
