package geopolitico.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Country {

    @JsonProperty("name")
    private Name name;

    @JsonProperty("population")
    private long population;

    @JsonProperty("area")
    private double area;

    @JsonProperty("capital")
    private List<String> capital;

    @JsonProperty("region")
    private String region;

    @JsonProperty("subregion")
    private String subregion;

    @JsonProperty("borders")
    private List<String> borders;

    /** @return nome comum do país, ou {@code null} se ausente na API */
    public String getCommonName() {
        return name != null ? name.common : null;
    }

    /** @return população absoluta; pode ser 0 se o dado não estiver disponível */
    public long getPopulation() {
        return population;
    }

    /** @return área em km²; pode ser 0.0 se o dado não estiver disponível */
    public double getArea() {
        return area;
    }

    /** @return lista (possivelmente vazia) de capitais */
    public List<String> getCapital() {
        return capital != null ? Collections.unmodifiableList(capital) : Collections.emptyList();
    }

    /** @return continente/região ("Africa", "Americas", "Asia", "Europe", "Oceania") */
    public String getRegion() {
        return region;
    }

    /** @return sub-região geográfica, ou {@code null} se ausente */
    public String getSubregion() {
        return subregion;
    }

    /** @return lista de códigos ISO3 dos países vizinhos (pode ser vazia) */
    public List<String> getBorders() {
        return borders != null ? Collections.unmodifiableList(borders) : Collections.emptyList();
    }

    public double getDensidade() {
        return area > 0 ? population / area : 0.0;
    }

    /** @return {@code true} quando o país possui dados mínimos utilizáveis */
    public boolean temDadosValidos() {
        return getCommonName() != null
                && population > 0
                && area > 0;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Name {
        @JsonProperty("common")
        public String common;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Country c)) return false;
        return Objects.equals(getCommonName(), c.getCommonName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCommonName());
    }

    @Override
    public String toString() {
        return "Country{name='%s', population=%d, area=%.1f, region='%s'}"
                .formatted(getCommonName(), population, area, region);
    }
}