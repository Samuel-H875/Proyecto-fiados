package fiados.servicio;

import fiados.evaluacion.EvaluadorFiado;
import fiados.evaluacion.RegistroEvaluadores;
import fiados.evaluacion.ResultadoEvaluacion;
import fiados.modelo.Cliente;
import fiados.modelo.Dinero;
import fiados.modelo.Movimiento;
import fiados.modelo.TipoMovimiento;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServicioFiados {

    private final RepositorioClientes repositorio;
    private final RegistroEvaluadores registro;
    private final Clock reloj;

    public ServicioFiados(RepositorioClientes repositorio, RegistroEvaluadores registro, Clock reloj) {
        this.repositorio = repositorio;
        this.registro = registro;
        this.reloj = reloj;
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    public List<EvaluadorFiado> tiposDisponibles() {
        return registro.listar();
    }

    public Cliente registrarCliente(String nombre, String tipo) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }
        EvaluadorFiado evaluador = registro.obtener(tipo);
        Cliente cliente = new Cliente(repositorio.siguienteId(), nombre.trim(), evaluador.getTipo());
        repositorio.guardar(cliente);
        return cliente;
    }

    public Cliente cambiarTipoCliente(int idCliente, String tipoNuevo) {
        Cliente cliente = buscarCliente(idCliente);
        EvaluadorFiado evaluador = registro.obtener(tipoNuevo);

        long saldo = cliente.getSaldo();
        long limite = evaluador.evaluar(cliente, 0, hoy()).getLimite();
        if (saldo > limite) {
            throw new IllegalArgumentException("No se puede cambiar a '" + evaluador.getTipo()
                    + "': el cliente debe " + Dinero.formatear(saldo) + " y el límite de ese tipo es "
                    + Dinero.formatear(limite) + ". Debe abonar al menos "
                    + Dinero.formatear(saldo - limite) + " antes del cambio.");
        }
        cliente.setTipo(evaluador.getTipo());
        return cliente;
    }

    public ResultadoEvaluacion registrarFiado(int idCliente, long monto, String detalle) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El valor de la compra debe ser mayor que cero.");
        }
        Cliente cliente = buscarCliente(idCliente);
        EvaluadorFiado evaluador = registro.obtener(cliente.getTipo());
        ResultadoEvaluacion resultado = evaluador.evaluar(cliente, monto, hoy());
        if (resultado.isAprobado()) {
            cliente.agregarMovimiento(new Movimiento(hoy(), TipoMovimiento.COMPRA_FIADA, monto, detalle));
        }
        return resultado;
    }

    public Movimiento registrarAbono(int idCliente, long monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El valor del abono debe ser mayor que cero.");
        }
        Cliente cliente = buscarCliente(idCliente);
        if (monto > cliente.getSaldo()) {
            throw new IllegalArgumentException("El abono (" + Dinero.formatear(monto)
                    + ") es mayor que el saldo pendiente (" + Dinero.formatear(cliente.getSaldo()) + ").");
        }
        Movimiento abono = new Movimiento(hoy(), TipoMovimiento.ABONO, monto, "");
        cliente.agregarMovimiento(abono);
        return abono;
    }

    public void registrarMovimientoHistorico(int idCliente, Movimiento movimiento) {
        buscarCliente(idCliente).agregarMovimiento(movimiento);
    }

    public Cliente buscarCliente(int idCliente) {
        return repositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("No existe un cliente con el número " + idCliente + "."));
    }

    public List<Cliente> listarClientes() {
        return repositorio.listar();
    }

    public List<Cliente> listarClientesConSaldo() {
        List<Cliente> conSaldo = new ArrayList<>();
        for (Cliente c : repositorio.listar()) {
            if (c.getSaldo() > 0) {
                conSaldo.add(c);
            }
        }
        return conSaldo;
    }

    public String estadoDeCuenta(int idCliente) {
        Cliente cliente = buscarCliente(idCliente);
        StringBuilder sb = new StringBuilder();
        sb.append("======== ESTADO DE CUENTA ========\n");
        sb.append(String.format("Cliente: %s (n.º %d) - tipo: %s%n", cliente.getNombre(), cliente.getId(), cliente.getTipo()));
        sb.append(String.format("%-11s %-13s %12s %12s  %s%n", "Fecha", "Movimiento", "Valor", "Saldo", "Detalle"));
        long saldo = 0;
        long fiado = 0;
        long abonado = 0;
        for (Movimiento m : cliente.getMovimientos()) {
            boolean esCompra = m.getTipo() == TipoMovimiento.COMPRA_FIADA;
            saldo += m.getTipo().aplicarASaldo(m.getValor());
            if (esCompra) {
                fiado += m.getValor();
            } else {
                abonado += m.getValor();
            }
            sb.append(String.format("%-11s %-13s %12s %12s  %s%n", m.getFecha(),
                    m.getTipo().getEtiqueta(),
                    Dinero.formatear(m.getValor()), Dinero.formatear(saldo), m.getDetalle()));
        }
        if (cliente.getMovimientos().isEmpty()) {
            sb.append("(sin movimientos)\n");
        }
        sb.append(String.format("Total fiado: %s | Total abonado: %s | SALDO PENDIENTE: %s%n",
                Dinero.formatear(fiado), Dinero.formatear(abonado), Dinero.formatear(cliente.getSaldo())));
        return sb.toString();
    }
}