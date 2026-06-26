package br.ufscar.dc.compiladores.autolang;

import br.ufscar.dc.compiladores.AutoLangParser;

import java.util.ArrayList;
import java.util.List;

// Simula cadeias sobre o automato validado, reportando aceitacao e o caminho.
public class Interpretador {

    private final Automato automato;
    private final StringBuilder saida = new StringBuilder();

    public Interpretador(Automato automato) {
        this.automato = automato;
    }

    public String getSaida() {
        return saida.toString();
    }

    private List<String> tokenizar(String cadeia) {
        List<String> simbolos = new ArrayList<>();
        for (int i = 0; i < cadeia.length(); i++) {
            simbolos.add(String.valueOf(cadeia.charAt(i)));
        }
        return simbolos;
    }

    public static class Resultado {
        public final boolean aceita;
        public final String rastro;
        public Resultado(boolean aceita, String rastro) {
            this.aceita = aceita;
            this.rastro = rastro;
        }
    }

    public Resultado simular(String cadeia) {
        StringBuilder rastro = new StringBuilder();
        String atual = automato.getEstadoInicial();
        rastro.append("(").append(atual).append(")");

        for (String sim : tokenizar(cadeia)) {
            if (!automato.getAlfabeto().contains(sim)) {
                rastro.append(" --").append(sim).append("--> [símbolo inválido]");
                return new Resultado(false, rastro.toString());
            }
            String prox = automato.destino(atual, sim);
            if (prox == null) {
                rastro.append(" --").append(sim).append("--> [sem transição]");
                return new Resultado(false, rastro.toString());
            }
            rastro.append(" --").append(sim).append("--> (").append(prox).append(")");
            atual = prox;
        }
        boolean aceita = automato.ehFinal(atual);
        rastro.append(aceita ? "  [final: ACEITA]" : "  [não-final: REJEITA]");
        return new Resultado(aceita, rastro.toString());
    }

    public String executar(AutoLangParser.ProgramaContext ctx) {
        saida.append("Automato: ").append(automato.getNome()).append("\n");
        saida.append("Alfabeto: ").append(automato.getAlfabeto()).append("\n");
        saida.append("Estados: ").append(automato.getEstados()).append("\n");
        saida.append("Inicial: ").append(automato.getEstadoInicial()).append("\n");
        saida.append("Finais: ").append(automato.getEstadosFinais()).append("\n");
        saida.append("Transicoes: ").append(automato.getTransicoes().size()).append("\n");
        saida.append("--------------------------------------------------\n");

        if (ctx.blocoTestar() == null || ctx.blocoTestar().casoTeste().isEmpty()) {
            saida.append("(nenhum caso de teste no bloco 'testar')\n");
            return saida.toString();
        }

        int total = 0, ok = 0;
        for (AutoLangParser.CasoTesteContext caso : ctx.blocoTestar().casoTeste()) {
            total++;
            String bruto = caso.CADEIA().getText();
            String cadeia = bruto.substring(1, bruto.length() - 1);
            boolean esperaAceita = caso.ACEITA() != null;

            Resultado r = simular(cadeia);
            boolean bateu = (r.aceita == esperaAceita);
            if (bateu) ok++;

            saida.append(String.format("Cadeia \"%s\": obtido=%s, esperado=%s -> %s%n",
                    cadeia,
                    r.aceita ? "aceita" : "rejeita",
                    esperaAceita ? "aceita" : "rejeita",
                    bateu ? "OK" : "FALHOU"));
            saida.append("   caminho: ").append(r.rastro).append("\n");
        }
        saida.append("--------------------------------------------------\n");
        saida.append(String.format("Resultado: %d/%d testes corretos%n", ok, total));
        return saida.toString();
    }
}
