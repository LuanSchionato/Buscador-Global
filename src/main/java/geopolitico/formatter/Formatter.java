package geopolitico.formatter;

import geopolitico.model.Country;
import geopolitico.model.GrupoResultado;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public final class Formatter {

    private static final List<String> REGIOES = List.of(
            "Africa", "Americas", "Asia", "Europe", "Oceania"
    );

    private static final Map<String, Double> AREA_CONTINENTE = Map.of(
            "Africa",   30_370_000.0,
            "Americas", 42_550_000.0,
            "Asia",     44_579_000.0,
            "Europe",   10_530_000.0,
            "Oceania",   8_525_989.0
    );

    private final PrintStream out;

    public Formatter(PrintStream out) {
        this.out = out;
    }

    public void exibirGrupo(GrupoResultado r) {
        out.println();
        out.println("=== " + r.label() + " ===");

        if (!r.temSubtracao()) {
            out.printf("Países    : %d%n", r.numeroPaises());
        }

        out.printf("População : %,d%n",       r.populacao());
        out.printf("Área      : %,.2f km²%n", r.area());

        if (r.area() > 0) {
            out.printf("Densidade : %.2f hab/km²%n", r.getDensidade());
        }

        if (!r.temValoresPositivos()) {
            out.println("     Atenção!: subtração resultou em valor(es) negativo(s).");
            out.println("     Os dados refletem apenas a diferença aritmética, não uma grandeza física.");
        }
    }

    public void exibirComparacao(GrupoResultado a, GrupoResultado b) {
        out.println();
        out.printf("=== %s  ×  %s ===%n", a.label(), b.label());
        out.println();

        String col0 = "%-16s";
        String col1 = "%-24s";
        String col2 = "%-24s";
        String fmt  = col0 + col1 + col2 + "%n";

        out.printf(fmt, "",             abreviar(a.label(), 22), abreviar(b.label(), 22));
        out.printf(fmt, "─".repeat(16), "─".repeat(24),         "─".repeat(24));

        if (!a.temSubtracao() && !b.temSubtracao()) {
            out.printf(fmt, "Países", a.numeroPaises(), b.numeroPaises());
        }

        out.printf(fmt, "População",
                String.format("%,d", a.populacao()),
                String.format("%,d", b.populacao()));

        out.printf(fmt, "Área (km²)",
                String.format("%,.0f", a.area()),
                String.format("%,.0f", b.area()));

        out.printf(fmt, "Densidade",
                String.format("%.2f", a.getDensidade()),
                String.format("%.2f", b.getDensidade()));

        out.println();
        out.println("── Proporções ──");
        compararProporcao("população", a.populacao(),        b.populacao(),        a.label(), b.label());
        compararProporcao("área",      (long) a.area(),      (long) b.area(),      a.label(), b.label());
        compararProporcaoDensidade(a.getDensidade(), b.getDensidade(), a.label(), b.label());
    }

    public void exibirLista(List<Country> lista) {
        out.println();
        out.printf("%-34s %-16s %-16s %-12s%n",
                "País", "População", "Área (km²)", "Densidade");
        out.println("─".repeat(78));

        for (Country c : lista) {
            String nome = c.getCommonName() != null ? c.getCommonName() : "(sem nome)";
            out.printf("%-34s %-16s %-16s %-12s%n",
                    abreviar(nome, 32),
                    String.format("%,d",   c.getPopulation()),
                    String.format("%,.0f", c.getArea()),
                    String.format("%.2f",  c.getDensidade()));
        }
    }

    public void exibirTotaisPorRegiao(List<Country> lista) {
        // Agrega população e contagem por região
        Map<String, long[]> totais = new LinkedHashMap<>();
        for (String r : REGIOES) totais.put(r, new long[]{0, 0}); // [pop, count]

        for (Country c : lista) {
            String r = c.getRegion();
            if (r != null && totais.containsKey(r)) {
                totais.get(r)[0] += c.getPopulation();
                totais.get(r)[1]++;
            }
        }

        out.println();
        out.println("=== Totais por Região ===");
        out.printf("%-12s %-8s %-18s %-16s %-12s%n",
                "Região", "Países", "População", "Área (km²)", "Densidade");
        out.println("─".repeat(68));

        for (String r : REGIOES) {
            long[] t = totais.get(r);
            if (t[1] == 0) continue;

            long   pop  = t[0];
            double area = AREA_CONTINENTE.getOrDefault(r, 0.0);
            double dens = area > 0 ? pop / area : 0.0;

            out.printf("%-12s %-8d %-18s %-16s %-12s%n",
                    r, t[1],
                    String.format("%,d",   pop),
                    String.format("%,.0f", area),
                    String.format("%.2f",  dens));
        }

        out.println("  * Área dos continentes: valor geográfico real, não soma dos países.");
    }

    public void exibirMenuPrincipal() {
        out.println();
        out.println("╔════════════════════════════════╗");
        out.println("║   Buscador Global — v2.0       ║");
        out.println("╠════════════════════════════════╣");
        out.println("║  1. Consultar / Comparar       ║");
        out.println("║  2. Listagem                   ║");
        out.println("║  0. Sair                       ║");
        out.println("╚════════════════════════════════╝");
        out.print("Escolha: ");
    }

    public void exibirMenuListagem() {
        out.println();
        out.println("=== Listagem ===");
        out.println("1. Lista completa");
        out.println("2. Lista condicional (com filtros)");
        out.print("Escolha: ");
    }

    public void exibirExemplosConsulta() {
        out.println();
        out.println("Exemplos de entrada:");
        out.println("  Brazil                                -> dados do Brasil");
        out.println("  Africa                                -> dados do continente africano");
        out.println("  Brazil Argentina                      -> soma Brasil + Argentina");
        out.println("  Americas - USA                        -> continente americano excluindo os EUA");
        out.println("  Africa India x China                  -> comparação: (África+Índia) vs China");
        out.println("  Asia - India x Asia - China           -> comparação: Asia excluindo India vs Asia excluindo China");
        out.print("Entrada: ");
    }

    public void exibirMenuOrdenacao() {
        out.println();
        out.println("Ordenar por: 1. População   2. Área   3. Densidade");
        out.print("Critério: ");
    }

    public void exibirMenuOrdem() {
        out.println("Ordem: 1. Crescente   2. Decrescente");
        out.print("Ordem: ");
    }

    public void exibirMenuFiltros() {
        out.println();
        out.println("── Filtros (pressione Enter para ignorar) ──");
    }

    public void erro(String mensagem) {
        out.println("  ✗ " + mensagem);
    }

    public void aviso(String mensagem) {
        out.println("  ⚠  " + mensagem);
    }

    private void compararProporcao(String campo, long va, long vb, String la, String lb) {
        if (va <= 0 || vb <= 0) return;
        if (va > vb)
            out.printf("  %s tem %.1fx mais %s que %s%n", la, (double) va / vb, campo, lb);
        else if (vb > va)
            out.printf("  %s tem %.1fx mais %s que %s%n", lb, (double) vb / va, campo, la);
        else
            out.printf("  %s: valores iguais.%n", campo);
    }

    private void compararProporcaoDensidade(double da, double db, String la, String lb) {
        if (da <= 0 || db <= 0) return;
        if (da > db)
            out.printf("  %s é %.1fx mais denso que %s%n", la, da / db, lb);
        else if (db > da)
            out.printf("  %s é %.1fx mais denso que %s%n", lb, db / da, la);
        else
            out.println("  Densidade: valores iguais.");
    }

    private static String abreviar(String texto, int limite) {
        if (texto == null) return "";
        return texto.length() <= limite ? texto : texto.substring(0, limite - 1) + "…";
    }
}
