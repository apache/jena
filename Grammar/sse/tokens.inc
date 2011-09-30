// Basic tokens for SPARQL / RDF terms.
// SSE - SPARQL S-Expressions
// Not the keywords.


TOKEN: { <WS: " " | "\t" | "\n" | "\r" | "\f"> }

#ifdef SKIP
SKIP : { <WS> }    //" " | "\t" | "\n" | "\r" | "\f" }
#endif

SPECIAL_TOKEN :
{
    <SINGLE_LINE_COMMENT1: "#" (~["\n","\r"])* ("\n"|"\r"|"\r\n")? > 
|
    // Lisp-style comments (makes Emacs lisp-mode more useful
    <SINGLE_LINE_COMMENT2: ";" (~["\n","\r"])* ("\n"|"\r"|"\r\n")? >
}

TOKEN:
{
   <IRIref: "<" (~[">","<","\u0000"-"\u0020"])* ">" >
|  <PNAME: (<PN_PREFIX>)? ":" (<PN_LOCAL>)? >
|  <BLANK_NODE_LABEL: "_:" (<PN_LOCAL>)? >  // Allows no label

    // Co-ordinate with ARQConstants
    // Named variable - allows no name
|  <VAR_NAMED:   "?" (<VARNAME>)?>

//     // Non-distinguished variable (BNode in SPARQL)
// |  <VAR_NAMED2:   "?." (~[" " , "\t" , "\n" , "\r" , "\f",
//                             "(", ")", "[", "]", "{", "}"])* >
// 
// |  <VAR_ANON:     "??" (~[ " " , "\t" , "\n" , "\r" , "\f",
//                            "(", ")", "[", "]", "{", "}"])* >

| <VAR_OTHER: "?" (<SYM>)+ >
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
| < ECHAR: "\\" ( "t"|"b"|"n"|"r"|"f"|"\\"|"\""|"'") >
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
}

// Specials for literals trailing parts
// Otherwise include in Symbol() rule for when out of position.
TOKEN :
{
  < DATATYPE: "^^" >
|  <LANGTAG: <AT> (<A2Z>)+("-" (<A2ZN>)+)* > : DEFAULT
| < #AT: "@">
|  <#A2Z: ["a"-"z","A"-"Z"]>
|  <#A2ZN: ["a"-"z","A"-"Z","0"-"9"]>
}

TOKEN:
{
  // XML 1.1 NCNameStartChar without "_"
  <#PN_CHARS_BASE:
          ["A"-"Z"] | ["a"-"z"] |
          ["\u00C0"-"\u00D6"] | ["\u00D8"-"\u00F6"] | ["\u00F8"-"\u02FF"] |
          ["\u0370"-"\u037D"] | ["\u037F"-"\u1FFF"] |
          ["\u200C"-"\u200D"] | ["\u2070"-"\u218F"] | ["\u2C00"-"\u2FEF"] |
          ["\u3001"-"\uD7FF"] | ["\uF900"-"\uFFFD"] 
          >
          // [#x10000-#xEFFFF]
|
  <#PN_CHARS_U: <PN_CHARS_BASE> | "_" >
|
// No DOT
  <#PN_CHARS: (<PN_CHARS_U> | "-" | ["0"-"9"] | "\u00B7" |
              ["\u0300"-"\u036F"] | ["\u203F"-"\u2040"] ) >
|
  // No leading "_", no trailing ".", can have dot inside prefix name.
  <#PN_PREFIX: <PN_CHARS_BASE> ((<PN_CHARS>|".")* <PN_CHARS>)?  >
|
  // With a leading "_", no dot at end of local name.
  <#PN_LOCAL: (<PN_CHARS_U> | ["0"-"9"]) ((<PN_CHARS>|".")* <PN_CHARS>)?  >
|
  // NCNAME without "-" and ".", allowing leading digits.
  <#VARNAME: ( <PN_CHARS_U> | ["0"-"9"] )
             ( <PN_CHARS_U> | ["0"-"9"] | "\u00B7" |
               ["\u0300"-"\u036F"] | ["\u203F"-"\u2040"] )* >


}

TOKEN:
{
  // Anything left that isn't structural
  // LPAREN and RPAREN / LBRACKET/RBRACKET
  // Quotes
  <#SYM:  (~["(", ")", "[", "]", "'", "\"", " ", "\t","\n","\r","\f" ])>
|
  <#SYM1: (~["^", "@", 
             "(", ")", "[", "]", "'", "\"", " ", "\t","\n","\r","\f" ])>
| <#SYM_ESC: "\\" ( " " | "'" | "\"" ) >
|
  <SYMBOL: <SYM1> (<SYM>)*>
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
