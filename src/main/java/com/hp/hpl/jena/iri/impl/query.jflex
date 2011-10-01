
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
    
    
    private int lastChar;
    char[] zzBuffer() {
     yyreset(null);
    this.zzAtEOF = true;
    int length = parser.end(range)-parser.start(range);
    lastChar = length - 1;
    zzEndRead = length;
    while (length > zzBuffer.length)
        zzBuffer = new char[zzBuffer.length*2];
      return zzBuffer;
    }
    
%}

%class LexerQuery
%%

// from query
[/?] { rule(1); }

// from pchar
[:@] { rule(2); }
