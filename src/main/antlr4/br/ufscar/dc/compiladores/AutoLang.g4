grammar AutoLang;

// DSL para definicao de Automatos Finitos Deterministicos (AFD).
// Um programa descreve um automato e, opcionalmente, cadeias a testar.

programa
    : 'automato' IDENT '{'
        blocoAlfabeto
        blocoEstados
        blocoInicial
        blocoFinais
        blocoTransicoes
        blocoTestar?
      '}' EOF
    ;

blocoAlfabeto
    : 'alfabeto' '{' simbolo (',' simbolo)* '}'
    ;

simbolo
    : IDENT
    | NUM
    ;

blocoEstados
    : 'estados' '{' IDENT (',' IDENT)* '}'
    ;

blocoInicial
    : 'inicial' IDENT
    ;

blocoFinais
    : 'finais' '{' (IDENT (',' IDENT)*)? '}'
    ;

blocoTransicoes
    : 'transicoes' '{' transicao* '}'
    ;

// origem -- simbolo --> destino
transicao
    : IDENT '--' simbolo '-->' IDENT
    ;

blocoTestar
    : 'testar' '{' casoTeste* '}'
    ;

casoTeste
    : CADEIA ('aceita' | 'rejeita')
    ;

// ===== Lexico =====

AUTOMATO   : 'automato' ;
ALFABETO   : 'alfabeto' ;
ESTADOS    : 'estados' ;
INICIAL    : 'inicial' ;
FINAIS     : 'finais' ;
TRANSICOES : 'transicoes' ;
TESTAR     : 'testar' ;
ACEITA     : 'aceita' ;
REJEITA    : 'rejeita' ;

SETA       : '-->' ;
TRACO      : '--' ;

ABRE_CH    : '{' ;
FECHA_CH   : '}' ;
VIRGULA    : ',' ;

IDENT      : [a-zA-Z_][a-zA-Z0-9_]* ;
NUM        : [0-9]+ ;

CADEIA     : '"' (~["\n\r])* '"' ;

// Cadeia sem aspas de fechamento: gera erro lexico
CADEIA_NAO_FECHADA : '"' (~["\n\r])* [\n\r] ;

COMENTARIO : '//' ~[\r\n]* -> skip ;
WS         : [ \t\r\n]+ -> skip ;
ERRO       : . ;
