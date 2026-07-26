package geopolitico.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import geopolitico.cache.InMemoryCache;
import geopolitico.exception.ApiException;
import geopolitico.model.Country;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RestCountriesClient implements CountryService {

    private static final String BASE_URL  = "https://restcountries.com/v3.1";
    private static final String FIELDS    = "name,population,area,region,capital,subregion,borders";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient    httpClient;
    private final ObjectMapper  objectMapper;
    private final InMemoryCache<List<Country>> cache;

    public RestCountriesClient() {
        this.httpClient   = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
        this.cache        = new InMemoryCache<>(32);
    }

    @Override
    public Optional<Country> buscarPais(String nome) {
        String chaveCache = "pais:" + nome.toLowerCase();
        var cached = cache.get(chaveCache);
        if (cached.isPresent()) {
            return cached.get().isEmpty()
                    ? Optional.empty()
                    : Optional.of(cached.get().get(0));
        }

        String nomeEncoded = URLEncoder.encode(nome, StandardCharsets.UTF_8);
        List<Country> candidates = buscarJson(BASE_URL + "/name/" + nomeEncoded + "?fields=" + FIELDS);

        for (Country c : candidates) {
            if (nome.equalsIgnoreCase(c.getCommonName())) {
                cache.put(chaveCache, List.of(c));
                return Optional.of(c);
            }
        }

        for (Country c : candidates) {
            if (c.getCommonName() != null
                    && c.getCommonName().toLowerCase().contains(nome.toLowerCase())) {
                cache.put(chaveCache, List.of(c));
                return Optional.of(c);
            }
        }

        if (!candidates.isEmpty()) {
            cache.put(chaveCache, List.of(candidates.get(0)));
            return Optional.of(candidates.get(0));
        }

        cache.put(chaveCache, List.of()); // armazena "não encontrado" também
        return Optional.empty();
    }

    @Override
    public List<Country> buscarTodos() {
        return cache.get("todos").orElseGet(() -> {
            List<Country> lista = buscarJson(BASE_URL + "/all?fields=" + FIELDS);
            cache.put("todos", lista);
            return lista;
        });
    }

    @Override
    public List<Country> buscarPorRegiao(String regiao) {
        String chaveCache = "regiao:" + regiao.toLowerCase();
        return cache.get(chaveCache).orElseGet(() -> {
            List<Country> lista = buscarJson(
                    BASE_URL + "/region/" + regiao + "?fields=" + FIELDS);
            cache.put(chaveCache, lista);
            return lista;
        });
    }

    private List<Country> buscarJson(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ApiException("Falha de conexão com a API RestCountries: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Requisição interrompida.", e);
        }

        if (response.statusCode() == 404) {
            return new ArrayList<>();
        }

        if (response.statusCode() != 200) {
            throw new ApiException("Resposta inesperada da API", response.statusCode());
        }

        try {
            return new ArrayList<>(objectMapper.readValue(
                    response.body(),
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, Country.class)));
        } catch (IOException e) {
            throw new ApiException("Erro ao processar resposta JSON da API: " + e.getMessage(), e);
        }
    }
}
