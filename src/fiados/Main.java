package fiados;

import fiados.evaluacion.EvaluadorClienteConfianza;
import fiados.evaluacion.EvaluadorClienteConocido;
import fiados.evaluacion.EvaluadorClienteNuevo;
import fiados.evaluacion.EvaluadorClienteQuincenal;
import fiados.evaluacion.RegistroEvaluadores;
import fiados.servicio.RepositorioClientes;
import fiados.servicio.ServicioFiados;
import fiados.ui.ConsolaFiados;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

public class Main {

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        RegistroEvaluadores registro = new RegistroEvaluadores();
        registro.registrar(new EvaluadorClienteNuevo());
        registro.registrar(new EvaluadorClienteConocido());
        registro.registrar(new EvaluadorClienteConfianza());
        registro.registrar(new EvaluadorClienteQuincenal());  

        ServicioFiados servicio = new ServicioFiados(new RepositorioClientes(), registro, Clock.systemDefaultZone());
        new ConsolaFiados(servicio).iniciar();
    }
}
