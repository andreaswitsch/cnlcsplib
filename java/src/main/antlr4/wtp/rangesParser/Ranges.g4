grammar Ranges;
prog:   (range)* ;
range:   VARSY ':' '[' (DOUBLE|INT) ',' (DOUBLE|INT) ']';
NEWLINE : [\r\n]+ -> skip;
INT     : [0-9]+ ;
DOUBLE  : [0-9]+'.'[0-9]* ;
VARSY   : [a-zA-Z]+[1-9]* ;
//WS      : [ \n\t\r];
WS      : [ \t\r\n]+ -> skip ;
