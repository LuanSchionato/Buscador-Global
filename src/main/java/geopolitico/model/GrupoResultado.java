package geopolitico.model;

public record GrupoResultado(
        String label,
        long populacao,
        double area,
        int numeroPaises,
        boolean temSubtracao
) {

    public double getDensidade() {
        return area > 0 ? (double) populacao / area : 0.0;
    }

    public boolean temValoresPositivos() {
        return populacao > 0 && area > 0;
    }
}
