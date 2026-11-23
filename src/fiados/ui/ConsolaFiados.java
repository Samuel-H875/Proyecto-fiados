package fiados.ui;

import fiados.evaluacion.EvaluadorFiado;
import fiados.evaluacion.ResultadoEvaluacion;
import fiados.modelo.Cliente;
import fiados.modelo.Dinero;
import fiados.modelo.TipoCliente;
import fiados.servicio.DatosEjemplo;
import fiados.servicio.ServicioFiados;
import java.util.List;
import java.util.Scanner;

public class ConsolaFiados {

    private final ServicioFiados servicio;
    private final Scanner entrada = new Scanner(System.in);

    public ConsolaFiados(ServicioFiados servicio) {
        this.servicio = servicio;
    }

    public void iniciar() {
        System.out.println("=== FIADOS - Tienda de barrio ===");
        try {
            boolean salir = false;
            while (!salir) {
                mostrarMenu();
                String opcion = leer("Opción: ");
                try {
                    switch (opcion) {
                        case "1" -> registrarCliente();
                        case "2" -> registrarFiado();
                        case "3" -> registrarAbono();
                        case "4" -> verEstadoDeCuenta();
                        case "5" -> listarClientes(false);
                        case "6" -> listarClientes(true);
                        case "7" -> cargarDatosEjemplo();
                        case "8" -> cambiarTipoCliente();
                        case "0" -> salir = true;
                        default -> System.out.println("Opción no válida.");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("No se pudo completar: " + e.getMessage());
                }
            }
        } catch (IllegalStateException finDeEntrada) {
        
        }
        System.out.println("Hasta luego.");
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("1. Registrar cliente");
        System.out.println("2. Registrar compra fiada");
        System.out.println("3. Registrar abono");
        System.out.println("4. Ver estado de cuenta de un cliente");
        System.out.println("5. Listar todos los clientes");
        System.out.println("6. Listar clientes con saldo pendiente");
        System.out.println("7. Cargar datos de ejemplo");
        System.out.println("8. Cambiar el tipo de un cliente");
        System.out.println("0. Salir");
    }

    private void registrarCliente() {
        String nombre = leer("Nombre del cliente: ");
        TipoCliente tipo = elegirTipo();
        Cliente c = servicio.registrarCliente(nombre, tipo.getClave());
        System.out.println("Cliente registrado: n.º " + c.getId() + " - " + c.getNombre() + " (" + c.getTipo() + ")");
    }

    private TipoCliente elegirTipo() {
        TipoCliente[] tipos = TipoCliente.values();
        System.out.println("Tipos de cliente disponibles:");
        for (int i = 0; i < tipos.length; i++) {
            System.out.println("  " + (i + 1) + ". " + tipos[i].getClave() + ": " + descripcionDe(tipos[i]));
        }
        while (true) {
            String texto = leer("Elija el tipo (1-" + tipos.length + "): ");
            try {
                int numero = Integer.parseInt(texto);
                if (numero >= 1 && numero <= tipos.length) {
                    return tipos[numero - 1];
                }
            } catch (NumberFormatException e) {
            }
            System.out.println("Opción no válida. Escriba un número entre 1 y " + tipos.length + ".");
        }
    }

    private String descripcionDe(TipoCliente tipo) {
        for (EvaluadorFiado evaluador : servicio.tiposDisponibles()) {
            if (evaluador.getTipo().equals(tipo.getClave())) {
                return evaluador.getDescripcionRegla();
            }
        }
        return "(sin regla registrada)";
    }

    private void cambiarTipoCliente() {
        int id = elegirCliente();
        Cliente cliente = servicio.buscarCliente(id);
        System.out.println("Tipo actual de " + cliente.getNombre() + ": " + cliente.getTipo());
        TipoCliente nuevo = elegirTipo();
        if (nuevo.getClave().equals(cliente.getTipo())) {
            System.out.println("El cliente ya es de tipo '" + cliente.getTipo() + "'. No se hizo ningún cambio.");
            return;
        }
        servicio.cambiarTipoCliente(id, nuevo.getClave());
        System.out.println("Tipo cambiado: " + cliente.getNombre() + " ahora es '" + cliente.getTipo() + "'.");
    }

    private void registrarFiado() {
        int id = elegirCliente();
        long monto = leerMonto("Valor de la compra: ");
        String detalle = leer("Productos (opcional, Enter para omitir): ");
        ResultadoEvaluacion r = servicio.registrarFiado(id, monto, detalle);
        System.out.println();
        if (r.isAprobado()) {
            System.out.println("FIADO APROBADO. " + r.getMotivo());
            System.out.println("Saldo anterior: " + Dinero.formatear(r.getSaldoActual())
                    + " | Saldo nuevo: " + Dinero.formatear(r.getSaldoNuevo()));
        } else {
            System.out.println("FIADO RECHAZADO. " + r.getMotivo());
            System.out.println("Saldo actual: " + Dinero.formatear(r.getSaldoActual())
                    + " | Límite: " + Dinero.formatear(r.getLimite()));
        }
    }

    private void registrarAbono() {
        int id = elegirCliente();
        System.out.println("Saldo pendiente: " + Dinero.formatear(servicio.buscarCliente(id).getSaldo()));
        long monto = leerMonto("Valor del abono: ");
        servicio.registrarAbono(id, monto);
        System.out.println("Abono registrado. Saldo que queda: "
                + Dinero.formatear(servicio.buscarCliente(id).getSaldo()));
    }

    private void verEstadoDeCuenta() {
        int id = elegirCliente();
        System.out.println();
        System.out.print(servicio.estadoDeCuenta(id));
    }

    private void listarClientes(boolean soloConSaldo) {
        List<Cliente> lista = soloConSaldo ? servicio.listarClientesConSaldo() : servicio.listarClientes();
        mostrarClientes(lista);
    }

    private void cargarDatosEjemplo() {
        DatosEjemplo.cargar(servicio);
        System.out.println("Datos de ejemplo cargados.");
        listarClientes(false);
    }

    private int elegirCliente() {
        mostrarClientes(servicio.listarClientes());
        return (int) leerMonto("Número del cliente: ");
    }

    private void mostrarClientes(List<Cliente> lista) {
        if (lista.isEmpty()) {
            System.out.println("(no hay clientes para mostrar)");
            return;
        }
        System.out.println(String.format("%-4s %-20s %-10s %14s", "N.º", "Nombre", "Tipo", "Saldo"));
        for (Cliente c : lista) {
            System.out.println(String.format("%-4d %-20s %-10s %14s", c.getId(), c.getNombre(), c.getTipo(),
                    Dinero.formatear(c.getSaldo())));
        }
    }

    private String leer(String mensaje) {
        System.out.print(mensaje);
        if (!entrada.hasNextLine()) {
            throw new IllegalStateException("Fin de la entrada");
        }
        return entrada.nextLine().trim();
    }

    private long leerMonto(String mensaje) {
        String texto = leer(mensaje).replace("$", "").replace(".", "").replace(",", "").replace(" ", "");
        try {
            return Long.parseLong(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debe escribir un número entero (por ejemplo 50000).");
        }
    }
}