package br.ufscar.dc.compiladores.autolang;

import br.ufscar.dc.compiladores.AutoLangBaseVisitor;
import br.ufscar.dc.compiladores.AutoLangParser;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AnalisadorSemantico extends AutoLangBaseVisitor<Void> {

    private final Automato automato = new Automato();
    private final List<String> erros = new ArrayList<>();
    private final List<String> avisos = new ArrayList<>();

    public Automato getAutomato() { return automato; }
    public List<String> getErros() { return erros; }
    public List<String> getAvisos() { return avisos; }
    public boolean temErros() { return !erros.isEmpty(); }

    private void erro(Token t, String msg) {
        erros.add("Linha " + t.getLine() + ": " + msg);
    }

    @Override
    public Void visitPrograma(AutoLangParser.ProgramaContext ctx) {
        automato.setNome(ctx.IDENT().getText());
        visit(ctx.blocoAlfabeto());
        visit(ctx.blocoEstados());
        visit(ctx.blocoInicial());
        visit(ctx.blocoFinais());
        visit(ctx.blocoTransicoes());
        verificarAlcancabilidade();
        return null;
    }

    @Override
    public Void visitBlocoAlfabeto(AutoLangParser.BlocoAlfabetoContext ctx) {
        Set<String> vistos = new HashSet<>();
        for (AutoLangParser.SimboloContext s : ctx.simbolo()) {
            String sim = s.getText();
            if (!vistos.add(sim)) {
                erro(s.getStart(), "símbolo '" + sim + "' declarado mais de uma vez no alfabeto");
            }
            automato.getAlfabeto().add(sim);
        }
        return null;
    }

    @Override
    public Void visitBlocoEstados(AutoLangParser.BlocoEstadosContext ctx) {
        Set<String> vistos = new HashSet<>();
        for (Token id : ctx.IDENT().stream().map(n -> n.getSymbol()).toArray(Token[]::new)) {
            String e = id.getText();
            if (!vistos.add(e)) {
                erro(id, "estado '" + e + "' declarado mais de uma vez");
            }
            automato.getEstados().add(e);
        }
        return null;
    }

    @Override
    public Void visitBlocoInicial(AutoLangParser.BlocoInicialContext ctx) {
        String ini = ctx.IDENT().getText();
        automato.setEstadoInicial(ini);
        if (!automato.getEstados().contains(ini)) {
            erro(ctx.IDENT().getSymbol(),
                 "estado inicial '" + ini + "' não foi declarado no bloco 'estados'");
        }
        return null;
    }

    @Override
    public Void visitBlocoFinais(AutoLangParser.BlocoFinaisContext ctx) {
        for (org.antlr.v4.runtime.tree.TerminalNode n : ctx.IDENT()) {
            String f = n.getText();
            if (!automato.getEstados().contains(f)) {
                erro(n.getSymbol(),
                     "estado final '" + f + "' não foi declarado no bloco 'estados'");
            }
            automato.getEstadosFinais().add(f);
        }
        return null;
    }

    @Override
    public Void visitBlocoTransicoes(AutoLangParser.BlocoTransicoesContext ctx) {
        Set<String> paresVistos = new HashSet<>();
        for (AutoLangParser.TransicaoContext t : ctx.transicao()) {
            String origem  = t.IDENT(0).getText();
            String simbolo = t.simbolo().getText();
            String destino = t.IDENT(1).getText();

            if (!automato.getEstados().contains(origem)) {
                erro(t.IDENT(0).getSymbol(), "estado de origem '" + origem + "' não declarado");
            }
            if (!automato.getEstados().contains(destino)) {
                erro(t.IDENT(1).getSymbol(), "estado de destino '" + destino + "' não declarado");
            }
            if (!automato.getAlfabeto().contains(simbolo)) {
                erro(t.simbolo().getStart(), "símbolo '" + simbolo + "' não pertence ao alfabeto");
            }

            String par = origem + "|" + simbolo;
            if (!paresVistos.add(par)) {
                erro(t.IDENT(0).getSymbol(),
                     "transição não-determinística: já existe uma transição de '"
                     + origem + "' com o símbolo '" + simbolo + "'");
            } else {
                automato.addTransicao(origem, simbolo, destino);
            }
        }
        return null;
    }

    // BFS a partir do inicial; estados nao atingidos viram aviso.
    private void verificarAlcancabilidade() {
        if (automato.getEstadoInicial() == null
                || !automato.getEstados().contains(automato.getEstadoInicial())) {
            return;
        }
        Set<String> alcancaveis = new LinkedHashSet<>();
        List<String> fila = new ArrayList<>();
        fila.add(automato.getEstadoInicial());
        alcancaveis.add(automato.getEstadoInicial());
        while (!fila.isEmpty()) {
            String atual = fila.remove(0);
            for (String sim : automato.getAlfabeto()) {
                String dest = automato.destino(atual, sim);
                if (dest != null && alcancaveis.add(dest)) {
                    fila.add(dest);
                }
            }
        }
        for (String e : automato.getEstados()) {
            if (!alcancaveis.contains(e)) {
                avisos.add("Aviso: estado '" + e + "' é inalcançável a partir do estado inicial");
            }
        }
    }
}
