import geopolitico.exception.ApiException;
import geopolitico.formatter.Formatter;
import geopolitico.model.Country;
import geopolitico.model.GrupoResultado;
import geopolitico.parser.ExpressionParser;
import geopolitico.parser.ExpressionParser.ParseException;
import geopolitico.service.CountryService;
import geopolitico.service.RestCountriesClient;
import geopolitico.util.InputUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {

    private final Scanner         scanner;
    private final CountryService  servico;
    private final ExpressionParser parser;
    private final Formatter        formatter;

    public Main() {
        this.scanner   = new Scanner(System.in);
        this.servico   = new RestCountriesClient();
        this.parser    = new ExpressionParser(servico);
        this.formatter = new Formatter(System.out);
    }

    Main(Scanner scanner, CountryService servico, Formatter formatter) {
        this.scanner   = scanner;
        this.servico   = servico;
        this.parser    = new ExpressionParser(servico);
        this.formatter = formatter;
    }

    public static void main(String[] args) {
        new Main().executar();
    }

    public void executar() {
        boolean rodando = true;
        while (rodando) {
            formatter.exibirMenuPrincipal();
            switch (scanner.nextLine().trim()) {
                case "1" -> consultarComparar();
                case "2" -> listagem();
                case "0" -> rodando = false;
                default  -> formatter.erro("Opção inválida.");
            }
        }
        System.out.println("\nAté logo!");
    }

    private void consultarComparar() {
        formatter.exibirExemplosConsulta();
        String entrada = scanner.nextLine().trim();
        if (entrada.isEmpty()) return;

        try {
            List<GrupoResultado> resultados = parser.analisar(entrada);

            if (resultados.size() == 2) {
                formatter.exibirComparacao(resultados.get(0), resultados.get(1));
            } else {
                formatter.exibirGrupo(resultados.get(0));
            }

        } catch (ParseException e) {
            formatter.erro(e.getMessage());
        } catch (ApiException e) {
            formatter.erro("Falha na comunicação com a API: " + e.getMessage());
        }
    }

    private void listagem() {
        formatter.exibirMenuListagem();
        switch (scanner.nextLine().trim()) {
            case "1" -> listaCompleta();
            case "2" -> listaCondicional();
            default  -> formatter.erro("Opção inválida.");
        }
    }

    private void listaCompleta() {
        try {
            formatter.exibirMenuOrdenacao();
            formatter.exibirMenuOrdem();
            int[] ord = InputUtils.lerOrdenacao(
                    scanner,
                    "Critério (1/2/3): ",
                    "Ordem (1/2): ");
            if (ord == null) return;

            List<Country> todos = new ArrayList<>(servico.buscarTodos());
            ordenar(todos, ord[0], ord[1]);
            formatter.exibirLista(todos);
            formatter.exibirTotaisPorRegiao(todos);

        } catch (ApiException e) {
            formatter.erro("Falha ao buscar dados: " + e.getMessage());
        }
    }

    private void listaCondicional() {
        formatter.exibirMenuFiltros();

        long   popMin  = InputUtils.lerLong  (scanner, "  População mínima   : ");
        double areaMin = InputUtils.lerDouble (scanner, "  Área mínima (km²)  : ");
        double densMin = InputUtils.lerDouble (scanner, "  Densidade mínima   : ");

        formatter.exibirMenuOrdenacao();
        int[] ord = InputUtils.lerOrdenacao(
                scanner,
                "Critério (1/2/3): ",
                "Ordem (1/2): ");
        if (ord == null) return;

        try {
            List<Country> filtrados = servico.buscarTodos().stream()
                    .filter(c -> c.getCommonName() != null)
                    .filter(c -> popMin  <= 0 || (c.getPopulation() > 0 && c.getPopulation()  >= popMin))
                    .filter(c -> areaMin <= 0 || (c.getArea()       > 0 && c.getArea()        >= areaMin))
                    .filter(c -> densMin <= 0 || (c.getArea()       > 0 && c.getDensidade()   >= densMin))
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

            if (filtrados.isEmpty()) {
                formatter.aviso("Nenhum país encontrado com esses filtros.");
                return;
            }

            ordenar(filtrados, ord[0], ord[1]);
            System.out.printf("%n  (%d países encontrados)%n", filtrados.size());
            formatter.exibirLista(filtrados);
            formatter.exibirTotaisPorRegiao(filtrados);

        } catch (ApiException e) {
            formatter.erro("Falha ao buscar dados: " + e.getMessage());
        }
    }

    private static void ordenar(List<Country> lista, int criterio, int ordem) {
        Comparator<Country> comp = switch (criterio) {
            case 1  -> Comparator.comparingLong(Country::getPopulation);
            case 2  -> Comparator.comparingDouble(Country::getArea);
            case 3  -> Comparator.comparingDouble(Country::getDensidade);
            default -> Comparator.comparing(c -> c.getCommonName() != null ? c.getCommonName() : "");
        };
        if (ordem == 2) comp = comp.reversed();
        lista.sort(comp);
    }
}
