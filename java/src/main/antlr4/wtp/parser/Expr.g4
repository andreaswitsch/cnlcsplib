grammar Expr;
prog:   (expr NEWLINE)* ;
expr:   '~' expr
    |   expr '^' expr
    |   expr ('*'|'/') expr
    |   expr ('+'|'-') expr
    |   expr ('<'|'>'|'<='|'>=') expr
    |   '-' expr
    |   INT
    |   VARSY
    |   'sqrt' '(' expr ')'
    |   'sin' '(' expr ')'
    |   'cos' '(' expr ')'
    |   'abs' '(' expr ')'
    |   'exp' '(' expr ')'
    |   'log' '(' expr ')'
    |   'atan2' '(' expr ',' expr ')'
    |   'min' '(' expr ',' expr ')'
    |   'max' '(' expr ',' expr ')'
    |   expr ('&&') expr
    |   expr ('and') expr
    |   expr ('||') expr
    |   expr ('or') expr
    |   '(' expr ')'
    ;
NEWLINE : [\r\n]+;
INT     : [0-9]+ ;
DOUBLE  : [0-9]+'.'[0-9]+ ;
VARSY   : [a-zA-Z]+[1-9]* ;
//WS      : [ \n\t\r];
WS      : [ \t\r\n]+ -> skip ;
