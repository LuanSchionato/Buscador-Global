package geopolitico.parser;

import geopolitico.exception.ApiException;
import geopolitico.model.Country;
import geopolitico.model.GrupoResultado;
import geopolitico.service.CountryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ExpressionParser {

    private static final Map<String, Double> AREA_CONTINENTE = Map.of(
            "Africa",   30_370_000.0,
            "Americas", 42_550_000.0,
            "Asia",     44_579_000.0,
            "Europe",   10_530_000.0,
            "Oceania",   8_525_989.0
    );

    private static final List<String> REGIOES = List.of(
            "Africa", "Americas", "Asia", "Europe", "Oceania"
    );

    private final CountryService servico;

    public ExpressionParser(CountryService servico) {
        this.servico = servico;
    }

    public List<GrupoResultado> analisar(String entrada) {
        String[] lados = entrada.trim().split("(?i)\\s+x\\s+", 2);

        if (lados.length == 2) {
            GrupoResultado a = resolverGrupo(lados[0].trim());
            GrupoResultado b = resolverGrupo(lados[1].trim());
            return List.of(a, b);
        }

        return List.of(resolverGrupo(entrada.trim()));
    }

    private GrupoResultado resolverGrupo(String expr) {
        List<String>  tokens       = new ArrayList<>();
        List<Boolean> somarFlags   = new ArrayList<>();
        boolean       temSubtracao = false;
        boolean       proximoSubtrai = false;

        for (String parte : expr.trim().split("\\s+")) {
            if (parte.equals("-")) {
                proximoSubtrai = true;
                temSubtracao   = true;
            } else {
                tokens.add(parte);
                somarFlags.add(!proximoSubtrai);
                proximoSubtrai = false;
            }
        }

        String label     = construirLabel(tokens, somarFlags);
        long   popTotal  = 0;
        double areaTotal = 0;
        int    numPaises = 0;

        for (int i = 0; i < tokens.size(); i++) {
            String  token = tokens.get(i);
            boolean somar = somarFlags.get(i);
            int     sinal = somar ? 1 : -1;

            Optional<String> regiao = matchRegiao(token);
            if (regiao.isPresent()) {
                List<Country> paises = servico.buscarPorRegiao(regiao.get());
                long   pop  = paises.stream().mapToLong(Country::getPopulation).sum();
                double area = AREA_CONTINENTE.getOrDefault(regiao.get(),
                        paises.stream().mapToDouble(Country::getArea).sum());
                popTotal  += sinal * pop;
                areaTotal += sinal * area;
                numPaises += sinal * paises.size();
            } else {
                Country pais = servico.buscarPais(token)
                        .orElseThrow(() -> new ParseException(
                                "Não encontrado: \"" + token + "\". Verifique o nome em inglês."));
                popTotal  += sinal * pais.getPopulation();
                areaTotal += sinal * pais.getArea();
                numPaises += sinal;
            }
        }

        return new GrupoResultado(label, popTotal, areaTotal, numPaises, temSubtracao);
    }

    private String construirLabel(List<String> tokens, List<Boolean> somarFlags) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0) sb.append(somarFlags.get(i) ? " + " : " - ");
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }

    private Optional<String> matchRegiao(String token) {
        return REGIOES.stream()
                .filter(r -> r.equalsIgnoreCase(token))
                .findFirst();
    }

    public static final class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }
}
