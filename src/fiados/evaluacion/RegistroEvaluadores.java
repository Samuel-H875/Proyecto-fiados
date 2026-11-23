package fiados.evaluacion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegistroEvaluadores {

    private final Map<String, EvaluadorFiado> evaluadores = new LinkedHashMap<>();

    public void registrar(EvaluadorFiado evaluador) {
        evaluadores.put(evaluador.getTipo().toLowerCase(), evaluador);
    }

    public EvaluadorFiado obtener(String tipo) {
        String clave = tipo == null ? "" : tipo.trim().toLowerCase();
        EvaluadorFiado evaluador = evaluadores.get(clave);
        if (evaluador == null) {
            throw new IllegalArgumentException("No existe el tipo de cliente '" + tipo + "'.");
        }
        return evaluador;
    }

    public List<EvaluadorFiado> listar() {
        return new ArrayList<>(evaluadores.values());
    }
}
