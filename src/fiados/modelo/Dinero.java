package fiados.modelo;

public final class Dinero {

    private Dinero() {
    }

    public static String formatear(long pesos) {
        String signo = pesos < 0 ? "-" : "";
        String digitos = String.valueOf(Math.abs(pesos));
        StringBuilder sb = new StringBuilder();
        int contador = 0;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            sb.append(digitos.charAt(i));
            contador++;
            if (contador % 3 == 0 && i > 0) {
                sb.append('.');
            }
        }
        return signo + "$" + sb.reverse();
    }
}
