package br.ufscar.dc.compiladores.autolang;

import br.ufscar.dc.compiladores.AutoLangLexer;
import br.ufscar.dc.compiladores.AutoLangParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

// Uso: java -jar autolang.jar <entrada.auto> [saida.txt]
public class Principal {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Uso: java -jar autolang.jar <entrada.auto> [saida.txt]");
            System.exit(1);
        }

        String arquivoEntrada = args[0];
        StringBuilder resultado = new StringBuilder();

        try {
            CharStream entrada = CharStreams.fromFileName(arquivoEntrada, StandardCharsets.UTF_8);

            AutoLangLexer lexer = new AutoLangLexer(entrada);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MeuErrorListener());
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            AutoLangParser parser = new AutoLangParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new MeuErrorListener());
            AutoLangParser.ProgramaContext arvore = parser.programa();

            AnalisadorSemantico sem = new AnalisadorSemantico();
            sem.visit(arvore);

            if (sem.temErros()) {
                resultado.append("Erros semanticos encontrados:\n");
                for (String e : sem.getErros()) {
                    resultado.append("  ").append(e).append("\n");
                }
            } else {
                for (String a : sem.getAvisos()) {
                    resultado.append(a).append("\n");
                }
                Interpretador interp = new Interpretador(sem.getAutomato());
                resultado.append(interp.executar(arvore));
            }

        } catch (MeuErrorListener.ErroSintatico ex) {
            resultado.append("Erro lexico/sintatico:\n  ").append(ex.getMessage()).append("\n");
        }

        if (args.length >= 2) {
            try (PrintWriter pw = new PrintWriter(
                    Files.newBufferedWriter(Paths.get(args[1]), StandardCharsets.UTF_8))) {
                pw.print(resultado);
            }
        } else {
            System.out.print(resultado);
        }
    }
}
