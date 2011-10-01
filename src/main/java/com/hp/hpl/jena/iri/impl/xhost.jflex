
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
    
    protected void error(int e) {
       switch(e) {
          case PERCENT:
          case PERCENT_20:
                parser.recordError(HOST,USE_PUNYCODE_NOT_PERCENTS);
            break;
        }
        parser.recordError(range,e);
    }
    
%}

%class LexerXHost
%%

[A-Z] { 
  rule(1); 
  error(LOWERCASE_PREFERRED); 
  }


