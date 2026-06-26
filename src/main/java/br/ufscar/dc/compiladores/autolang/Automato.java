package br.ufscar.dc.compiladores.autolang;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Automato {

    private String nome;
    private final Set<String> alfabeto = new LinkedHashSet<>();
    private final Set<String> estados = new LinkedHashSet<>();
    private String estadoInicial;
    private final Set<String> estadosFinais = new LinkedHashSet<>();

    // (origem, simbolo) -> destino. Chave: "origem|simbolo".
    private final Map<String, String> transicoes = new LinkedHashMap<>();

    public static String chave(String origem, String simbolo) {
        return origem + "|" + simbolo;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Set<String> getAlfabeto() { return alfabeto; }
    public Set<String> getEstados() { return estados; }

    public String getEstadoInicial() { return estadoInicial; }
    public void setEstadoInicial(String estadoInicial) { this.estadoInicial = estadoInicial; }

    public Set<String> getEstadosFinais() { return estadosFinais; }
    public Map<String, String> getTransicoes() { return transicoes; }

    public void addTransicao(String origem, String simbolo, String destino) {
        transicoes.put(chave(origem, simbolo), destino);
    }

    public String destino(String origem, String simbolo) {
        return transicoes.get(chave(origem, simbolo));
    }

    public boolean ehFinal(String estado) {
        return estadosFinais.contains(estado);
    }
}
