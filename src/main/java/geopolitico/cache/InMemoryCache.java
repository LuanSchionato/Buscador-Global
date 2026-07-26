package geopolitico.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryCache<V> {

    private final Map<String, V> store;

    public InMemoryCache(int capacidadeMaxima) {
        this.store = Collections.synchronizedMap(
                new LinkedHashMap<>(16, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, V> eldest) {
                        return size() > capacidadeMaxima;
                    }
                }
        );
    }

    public Optional<V> get(String chave) {
        return Optional.ofNullable(store.get(chave));
    }

    public void put(String chave, V valor) {
        store.put(chave, valor);
    }

    public void limpar() {
        store.clear();
    }

    public int tamanho() {
        return store.size();
    }
}
