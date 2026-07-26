package geopolitico.service;

import geopolitico.model.Country;

import java.util.List;
import java.util.Optional;

public interface CountryService {

    Optional<Country> buscarPais(String nome);

    List<Country> buscarTodos();

    List<Country> buscarPorRegiao(String regiao);
}
