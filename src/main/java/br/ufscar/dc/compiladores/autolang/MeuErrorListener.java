package br.ufscar.dc.compiladores.autolang;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class MeuErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        throw new ErroSintatico("Linha " + line + ": " + msg);
    }

    public static class ErroSintatico extends RuntimeException {
        public ErroSintatico(String mensagem) {
            super(mensagem);
        }
    }
}
