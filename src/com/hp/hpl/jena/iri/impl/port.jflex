
package com.hp.hpl.jena.iri.impl;
%%


%unicode
%integer
%char
%implements com.hp.hpl.jena.iri.ViolationCodes
%implements com.hp.hpl.jena.iri.IRIComponents
%implements Lexer
%extends AbsLexer
%buffer 2048


%{
    
    
    
    char[] zzBuffer() {
     yyreset(null);
    this.zzAtEOF = true;
    int length = parser.end(range)-parser.start(range);
    zzEndRead = length;
    while (length > zzBuffer.length)
        zzBuffer = new char[zzBuffer.length*2];
    if (length==0)
           error(PORT_SHOULD_NOT_BE_EMPTY);
      return zzBuffer;
    }
    
%}
%class LexerPort
%%
[1-9] {
rule(1); 
}

^0 {
rule(2); 
if (yychar==0) error(PORT_SHOULD_NOT_START_IN_ZERO);
}

0 {
rule(3); 
}
[^1] {
rule(4); error(ILLEGAL_CHARACTER);}

