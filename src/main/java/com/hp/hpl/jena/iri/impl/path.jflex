
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

%class LexerPath
%%

// TODO path absolute

^([.][.][/])+ { rule(2); }

[:@] { rule(3); }

[/][.][.][/] { rule(4); error(NON_INITIAL_DOT_SEGMENT); }

[/][.][/] { rule(5); error(NON_INITIAL_DOT_SEGMENT); }

[/.] { rule(6); }

[/][.][.] { rule(7); if (yychar == lastChar-2)  error(NON_INITIAL_DOT_SEGMENT); }

[/][.] { rule(8); if (yychar == lastChar-1) error(NON_INITIAL_DOT_SEGMENT); }



