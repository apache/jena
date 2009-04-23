/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.tokens;

import static com.hp.hpl.jena.riot.Chars.CH_COMMA;
import static com.hp.hpl.jena.riot.Chars.CH_DOT;
import static com.hp.hpl.jena.riot.Chars.CH_LBRACE;
import static com.hp.hpl.jena.riot.Chars.CH_LBRACKET;
import static com.hp.hpl.jena.riot.Chars.CH_LPAREN;
import static com.hp.hpl.jena.riot.Chars.CH_RBRACE;
import static com.hp.hpl.jena.riot.Chars.CH_RBRACKET;
import static com.hp.hpl.jena.riot.Chars.CH_RPAREN;
import static com.hp.hpl.jena.riot.Chars.CH_SEMICOLON;
import static com.hp.hpl.jena.riot.tokens.TokenType.BNODE;
import static com.hp.hpl.jena.riot.tokens.TokenType.DECIMAL;
import static com.hp.hpl.jena.riot.tokens.TokenType.DOUBLE;
import static com.hp.hpl.jena.riot.tokens.TokenType.INTEGER;
import static com.hp.hpl.jena.riot.tokens.TokenType.IRI;
import static com.hp.hpl.jena.riot.tokens.TokenType.LITERAL_DT;
import static com.hp.hpl.jena.riot.tokens.TokenType.LITERAL_LANG;
import static com.hp.hpl.jena.riot.tokens.TokenType.STRING2;
import static com.hp.hpl.jena.riot.tokens.TokenType.VAR;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.riot.RiotException;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.vocabulary.XSD;

public final class Token
{
    private TokenType tokenType = null ;
    private String tokenImage = null ;
    private String tokenImage2 = null ;
    private Token subToken = null ;     // A related token (used for datatype literals)
    public int cntrlCode = 0 ;
    private long column ;
    private long line ;
    
    public final TokenType getType() { return tokenType ; }
    public final String getImage()   { return tokenImage ; }
    public final String getImage2()  { return tokenImage2 ; }
    public final int getCntrlCode()  { return cntrlCode ; }
    public final Token getSubToken() { return subToken ; }
    
    public final void setType(TokenType tokenType)    { this.tokenType = tokenType ; }
    public final void setImage(String tokenImage)     { this.tokenImage = tokenImage ; }
    public final void setImage2(String tokenImage2)   { this.tokenImage2 = tokenImage2 ; }
    public final void setCntrlCode(int cntrlCode)     { this.cntrlCode = cntrlCode ; }
    public final void setSubToken(Token subToken)     { this.subToken = subToken ; }
    
    public long getColumn()
    {
        return column ;
    }

    public long getLine()
    {
        return line ;
    }
    
    private Token(TokenType type) { this(type, null, null) ; }
    
    private Token(TokenType type, String image1) { this(type, image1, null) ; }
    
    private Token(TokenType type, String image1, String image2)
    { 
        this() ;
        setType(type) ;
        setImage(image1) ;
        setImage2(image2) ;
    }
    
    private Token() { this(-1, -1) ; }
    
    public Token(long line, long column) { this.line = line ; this.column = column ; }
    
    public Token(Token token)
    { 
        this.tokenType = token.tokenType ;
        this.tokenImage = token.tokenImage ;
        this.tokenImage2 = token.tokenImage2 ;
        this.cntrlCode = token.cntrlCode ;
        this.line = token.line ; 
        this.column = token.column ;
    }
    
    public String asWord()
    {
        if ( ! hasType(TokenType.KEYWORD) ) return null ;
        return getImage() ; 
    }
    
    public String text()
    {
        return toString(false) ;
        
    }
    
    @Override
    public String toString()
    {
        return toString(false) ;
    }
     
    static final String delim = "|" ;
    public String toString(boolean addLocation)
    {
        StringBuilder sb = new StringBuilder() ;
        if ( addLocation && getLine() >= 0 && getColumn() >= 0 )
            sb.append(String.format("[%d,%d]", getLine(), getColumn())) ;
        sb.append("[") ;
        sb.append(getType().toString()) ;
        
        
        if ( getImage() != null )
        {
            sb.append(":") ;
            sb.append(delim) ;
            sb.append(getImage()) ;
            sb.append(delim) ;
            
            if ( getImage2() != null )
            {
                sb.append(":") ;
                sb.append(delim) ;
                sb.append(getImage2()) ;
                sb.append(delim) ;
            }
            if ( getSubToken() != null )
            {
                sb.append(";") ;
                sb.append(delim) ;
                sb.append(getSubToken().toString()) ;
                sb.append(delim) ;
            }   

        }
        if ( getCntrlCode() != 0 )
        {
            sb.append(":") ; 
            sb.append(getCntrlCode()) ;
        }
        sb.append("]") ;
        return sb.toString() ;
    }
    

    public boolean isWord() { return tokenType == TokenType.KEYWORD ; }

    public boolean isNode()
    {
        switch(tokenType)
        {
            case BNODE :
            case IRI : 
            case PREFIXED_NAME :
            case DECIMAL: 
            case DOUBLE:
            case INTEGER:
            case LITERAL_DT:
            case LITERAL_LANG:
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
                return true ;
            default:
                return false ;
        }
    }
    
    // N-Triples but allows single quoted strings as well.
    public boolean isNodeBasic()
    {
        switch(tokenType)
        {
            case BNODE :
            case IRI : 
            case PREFIXED_NAME :
            case LITERAL_DT:
            case LITERAL_LANG:
            case STRING1:
            case STRING2:
                return true ;
            default:
                return false ;
        }
    }
    
    public boolean isBasicLiteral()
    {
        switch(tokenType)
        {
            case LITERAL_DT:
            case LITERAL_LANG:
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
                return true ;
            default:
                return false ;
        }
    }
    
    // Validation of URIs?
    
    public Node asNode()
    {
        switch(tokenType)
        {
            case BNODE : return Node.createAnon(new AnonId(tokenImage)) ;
            case IRI :   return Node.createURI(tokenImage) ; 
            case PREFIXED_NAME :
                return Node.createURI("urn:prefixed-name:"+tokenImage+":"+tokenImage2) ;
            case DECIMAL :  return Node.createLiteral(tokenImage, null, XSDDatatype.XSDdecimal)  ; 
            case DOUBLE :   return Node.createLiteral(tokenImage, null, XSDDatatype.XSDdouble)  ;
            case INTEGER:   return Node.createLiteral(tokenImage, null, XSDDatatype.XSDinteger) ;
            case LITERAL_DT :
            {
                Node n = getSubToken().asNode();
                if ( ! n.isURI() )
                    throw new RiotException("Invalid token: "+this) ;
                RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(n.getURI()) ;
                return Node.createLiteral(tokenImage, null, dt)  ;
            }
            case LITERAL_LANG : return Node.createLiteral(tokenImage, tokenImage2, null)  ;
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
                return Node.createLiteral(tokenImage) ;
            
            default: break ;
        }
        return null ;
    }
    
    public boolean hasType(TokenType tokenType)
    {
        return getType() == tokenType ;
    }
    
    @Override
    public int hashCode()
    {
        return Utils.hashCodeObject(tokenType) ^
                Utils.hashCodeObject(tokenImage) ^
                Utils.hashCodeObject(tokenImage2) ^
                Utils.hashCodeObject(cntrlCode) ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof Token ) ) return false ;
        Token t = (Token)other ;
        return  Utils.equals(tokenType, t.tokenType) &&
                Utils.equals(tokenImage, t.tokenImage) &&
                Utils.equals(tokenImage2, t.tokenImage2) &&
                Utils.equals(cntrlCode, t.cntrlCode) ;
    }
    
    public static Token tokenForChar(char character)
    {
        switch(character)
        { 
            case CH_DOT:        return new Token(TokenType.DOT) ;
            case CH_SEMICOLON:  return new Token(TokenType.SEMICOLON) ;
            case CH_COMMA:      return new Token(TokenType.COMMA) ;
            case CH_LBRACE:     return new Token(TokenType.LBRACE) ;
            case CH_RBRACE:     return new Token(TokenType.RBRACE) ;
            case CH_LPAREN:     return new Token(TokenType.LPAREN) ;
            case CH_RPAREN:     return new Token(TokenType.RPAREN) ;
            case CH_LBRACKET:   return new Token(TokenType.LBRACKET) ;
            case CH_RBRACKET:   return new Token(TokenType.RBRACKET) ;
            default:
                throw new RuntimeException("Token error: unrecognized charcater: "+character) ;
        }
    }
    
    public static Token tokenForWord(String word)
    {
        return new Token(TokenType.KEYWORD, word) ; 
    }
    
    public static Token tokenForNode(Node n)
        {
            if ( n.isURI() )
                // Prefixing.
                return new Token(IRI, n.getURI()) ;
            if ( n.isBlank() )
                return new Token(BNODE, n.getBlankNodeLabel()) ;
            if ( n.isVariable() )
                return new Token(VAR, n.getName()) ;
            if ( n.isLiteral() )
            {
                String datatype = n.getLiteralDatatypeURI() ;
                String lang = n.getLiteralLanguage() ;
                String s = n.getLiteralLexicalForm() ;
                
                if ( datatype != null )
                {
                    // Special form we know how to handle?
                    // Assume valid text
                    if ( datatype.equals(XSD.integer.getURI()) )
                    {
                        try {
                            String s1 = s ;
                            // BigInteger does not allow leading +
                            // so chop it off before the format test
                            // BigDecimal does allow a leading +
                            if ( s.startsWith("+") )
                                s1 = s.substring(1) ;
                            new java.math.BigInteger(s1) ;
                            return new Token(INTEGER, s) ;
                        } catch (NumberFormatException nfe) {}
                        // No luck.  Continue.
                        // Continuing is always safe.
                    }
                    
                    if ( datatype.equals(XSD.decimal.getURI()) )
                    {
                        if ( s.indexOf('.') > 0 )
                        {
                            try {
                                // BigDecimal does allow a leading +
                                new java.math.BigDecimal(s) ;
                                return new Token(DECIMAL, s) ;
                            } catch (NumberFormatException nfe) {}
                            // No luck.  Continue.
                        }
                    }
                    
                    if ( datatype.equals(XSD.xdouble.getURI()) )
                    {
                        // Assumes SPARQL has decimals and doubles.
                        // Must have 'e' or 'E' to be a double short form.
    
                        if ( s.indexOf('e') >= 0 || s.indexOf('E') >= 0 )
                        {
                            try {
                                Double.parseDouble(s) ;
                                return new Token(DOUBLE, s) ; 
                            } catch (NumberFormatException nfe) {}
                            // No luck.  Continue.
                        }
                    }
    
    //                if ( datatype.equals(XSD.xboolean.getURI()) )
    //                {
    //                    if ( s.equalsIgnoreCase("true") ) return new Token(BOOLEAN, s) ;
    //                    if ( s.equalsIgnoreCase("false") ) return new Token(BOOLEAN, s) ;
    //                }
                    // Not a recognized form.
                    // Has datatype.
                    return new Token(LITERAL_DT, s, datatype) ;    // WRONG-ish
                }
    
                if ( lang != null && lang.length()>0)
                    return new Token(LITERAL_LANG, s, lang) ; 
                
                // Plain.
                return new Token(STRING2, s) ; 
            }
            
            throw new IllegalArgumentException() ;
            
        }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */