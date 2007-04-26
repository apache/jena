// (c) Copyright 2007 Hewlett-Packard Development Company, LP
// Basic tokens for SPARQL / RDF terms.
// SSE - SPARQL S-Expressions
// Not the keywords.

#ifdef SKIP
SKIP : { " " | "\t" | "\n" | "\r" | "\f" }
#else
TOKEN: { <WS: " " | "\t" | "\n" | "\r" | "\f"> }
#endif

SPECIAL_TOKEN :
{ <SINGLE_LINE_COMMENT: "#" (~["\n","\r"])* ("\n"|"\r"|"\r\n")? > }

TOKEN:
{
   <Q_IRIref: "<" (~[">","<","\u0000"-"\u0020"])* ">" >
|  <QNAME: (<NCNAME_PREFIX>)? ":" (<NCNAME>)? >
|  <BLANK_NODE_LABEL: "_:" <NCNAME> >
|  <VAR1: <HOOK> <VARNAME> >
|  <VAR2: "$" <VARNAME> >
|  <LANGTAG: <AT> (<A2Z>)+("-" (<A2ZN>)+)* >
|  <#A2Z: ["a"-"z","A"-"Z"]>
|  <#A2ZN: ["a"-"z","A"-"Z","0"-"9"]>
}

TOKEN :
{
  < #DIGITS: (["0"-"9"])+>
| < INTEGER: (["+","-"])? <DIGITS> >
| < DECIMAL: (["+","-"])? ( <DIGITS> "." (<DIGITS>)* | "." <DIGITS> ) >
| < DOUBLE:   // Required exponent.
      ( (["+","-"])? 
        (["0"-"9"])+ "." (["0"-"9"])* <EXPONENT>
        | "." (["0"-"9"])+ (<EXPONENT>)
        | (["0"-"9"])+ <EXPONENT>
      )
      >
| < #EXPONENT: ["e","E"] (["+","-"])? (["0"-"9"])+ >
| < #QUOTE_3D: "\"\"\"">
| < #QUOTE_3S: "'''">
| <ECHAR: "\\" ( "t"|"b"|"n"|"r"|"f"|"\\"|"\""|"'") >
| < STRING_LITERAL1: 
      // Single quoted string
      "'" ( (~["'","\\","\n","\r"]) | <ECHAR> )* "'" > 
| < STRING_LITERAL2:
    // Double quoted string
      "\"" ( (~["\"","\\","\n","\r"]) | <ECHAR> )* "\"" >
| < STRING_LITERAL_LONG1:
     <QUOTE_3S> 
      ( ("'" | "''")? (~["'","\\"] | <ECHAR> ))*
     <QUOTE_3S> >

| < STRING_LITERAL_LONG2: 
     <QUOTE_3D> 
      ( ("\"" | "\"\"")? (~["\"","\\"] | <ECHAR> ))*
     <QUOTE_3D> >
}

TOKEN :
{
  < LPAREN:    "(" >
| < RPAREN:    ")" >


| < LBRACE:    "{" >
| < RBRACE:    "}" >

| < LBRACKET:  "[" >
| < RBRACKET:  "]" >

| < DATATYPE: "^^">
| < #AT: "@">

| < HOOK:      "?" >
}

TOKEN:
{
  // XML 1.1 NCNameStartChar without "_"
  <#NCCHAR1P:
          ["A"-"Z"] | ["a"-"z"] |
          ["\u00C0"-"\u00D6"] | ["\u00D8"-"\u00F6"] | ["\u00F8"-"\u02FF"] |
          ["\u0370"-"\u037D"] | ["\u037F"-"\u1FFF"] |
          ["\u200C"-"\u200D"] | ["\u2070"-"\u218F"] | ["\u2C00"-"\u2FEF"] |
          ["\u3001"-"\uD7FF"] | ["\uF900"-"\uFFFD"] 
          >
          // [#x10000-#xEFFFF]
|
  <#NCCHAR1: <NCCHAR1P> | "_" >

// No trailing DOTs in qnames.
|
  // #NCCHAR without "."
  <#NCCHAR: (<NCCHAR1> | "-" | ["0"-"9"] | "\u00B7" |
              ["\u0300"-"\u036F"] | ["\u203F"-"\u2040"] ) >
|
  // NCNAME but no leading "_", no trailing ".", can have dot inside prefix name.
  <#NCNAME_PREFIX: <NCCHAR1P> ((<NCCHAR>|".")* <NCCHAR>)?  >
|
  // With a leading "_", no dot at end of local name.
  <#NCNAME: <NCCHAR1> ((<NCCHAR>|".")* <NCCHAR>)?  >
|
  // NCNAME without "-" and ".", allowing leading digits.
  <#VARNAME: ( <NCCHAR1> | ["0"-"9"] )
             ( <NCCHAR1> | ["0"-"9"] | "\u00B7" |
               ["\u0300"-"\u036F"] | ["\u203F"-"\u2040"] )* >
}

// Can we add @foo?
TOKEN:
{
  <WORD: <VARNAME> >
  // Anything that isn't structural
  // LPAREN and RPAREN / LBRACKET/RBRACKET
  // Quotes
| <OP: (~["(", ")", "[", "]", "'", "\"", "\t","\n","\r","\f" ])+ >
}

// Catch-all tokens.  Must be last.  
// Any non-whitespace.  Causes a parser exception, rather than a
// token manager error (with hidden line numbers).
// Only bad IRIs (e.g. spaces) now give unhelpful parse errors.
TOKEN:
{
  <#UNKNOWN: (~[" ","\t","\n","\r","\f" ])+ >
}

/*
# Local Variables:
# tab-width: 4
# indent-tabs-mode: nil
# comment-default-style: "//"
# End:
*/
