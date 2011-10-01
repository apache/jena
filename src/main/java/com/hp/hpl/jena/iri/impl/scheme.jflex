
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
           error(EMPTY_SCHEME);
      return zzBuffer;
    }
    
%}


%class LexerScheme
%%

[a-z] {
 rule(1);
}
[A-Z] {
 rule(2); 
 error(LOWERCASE_PREFERRED);
}

^[+.0-9] {
 rule(3); 
 if (yychar==0) error(SCHEME_MUST_START_WITH_LETTER);
}

^- {
 rule(4); 
 if (yychar==0) error(SCHEME_MUST_START_WITH_LETTER);
 error(SCHEME_INCLUDES_DASH);
}
- {
 rule(5); error(SCHEME_INCLUDES_DASH);
}

[+.0-9] {
 rule(6);
}

[^a] {
rule(7); 
error(ILLEGAL_CHARACTER);
}

