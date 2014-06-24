/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.riot.tokens;

import static org.apache.jena.atlas.lib.Chars.CH_COMMA ;
import static org.apache.jena.atlas.lib.Chars.CH_DOT ;
import static org.apache.jena.atlas.lib.Chars.CH_LBRACE ;
import static org.apache.jena.atlas.lib.Chars.CH_LBRACKET ;
import static org.apache.jena.atlas.lib.Chars.CH_LPAREN ;
import static org.apache.jena.atlas.lib.Chars.CH_RBRACE ;
import static org.apache.jena.atlas.lib.Chars.CH_RBRACKET ;
import static org.apache.jena.atlas.lib.Chars.CH_RPAREN ;
import static org.apache.jena.atlas.lib.Chars.CH_SEMICOLON ;
import static org.apache.jena.atlas.lib.Lib.equal ;
import static org.apache.jena.atlas.lib.Lib.hashCodeObject ;
import static org.apache.jena.riot.tokens.TokenType.BNODE ;
import static org.apache.jena.riot.tokens.TokenType.DECIMAL ;
import static org.apache.jena.riot.tokens.TokenType.DOUBLE ;
import static org.apache.jena.riot.tokens.TokenType.INTEGER ;
import static org.apache.jena.riot.tokens.TokenType.IRI ;
import static org.apache.jena.riot.tokens.TokenType.LITERAL_DT ;
import static org.apache.jena.riot.tokens.TokenType.LITERAL_LANG ;
import static org.apache.jena.riot.tokens.TokenType.STRING ;
import static org.apache.jena.riot.tokens.TokenType.VAR ;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prologue ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.vocabulary.XSD ;

public final class Token
{
    // Some tokens are "multipart"
    //   A language tag is a sub-token string and token part.
    //     It uses subToken1, and image2.
    //   A datatype literal is two tokens
    //     It uses subToken1, subToken2 and sets image to the lexical part.
    //   A prefixed name is two strings. 
    //     It uses tokenImage and tokenImage2
    
    private TokenType tokenType = null ;
    
    private String tokenImage = null ;
    private String tokenImage2 = null ;         // Used for language tag and second part of prefix name
    
    private Token subToken1 = null ;            // A related token (used for datatype literals and language tags)
    private Token subToken2 = null ;            // A related token (used for datatype literals and language tags)
    
    public int cntrlCode = 0 ;
    private long column ;
    private long line ;
    
    // Keywords recognized.
    public static final String ImageANY     = "ANY" ;
    public static final String ImageTrue    = "true" ;
    public static final String ImageFalse   = "false" ;
    
    public final TokenType getType()    { return tokenType ; }
    public final String getImage()      { return tokenImage ; }
    //public final String getImage1()  { return tokenImage1 ; }
    
    public final String getImage2()     { return tokenImage2 ; }
    public final int getCntrlCode()     { return cntrlCode ; }
    public final Token getSubToken1()   { return subToken1 ; }
    public final Token getSubToken2()   { return subToken2 ; }
    
    public final Token setType(TokenType tokenType)     { this.tokenType = tokenType ; return this ; }
    public final Token setImage(String tokenImage)      { this.tokenImage = tokenImage ; return this ; }
    public final Token setImage(char tokenImage)        { this.tokenImage = String.valueOf(tokenImage) ; return this ; }
    
    public final Token setImage2(String tokenImage2)    { this.tokenImage2 = tokenImage2 ; return this ; }
    
    public final Token setCntrlCode(int cntrlCode)      { this.cntrlCode = cntrlCode ; return this ; }

    public final Token setSubToken1(Token subToken)      { this.subToken1 = subToken ; return this ; }
    public final Token setSubToken2(Token subToken)      { this.subToken2 = subToken ; return this ; }
    
    static Token create(String s)
    {
        PeekReader pr = PeekReader.readString(s) ;
        TokenizerText tt = new TokenizerText(pr) ;
        if ( ! tt.hasNext() )
            throw new RiotException("No token") ;
        Token t = tt.next() ;
        if ( tt.hasNext() )
            throw new RiotException("Extraneous charcaters") ;
        return t ;
    }

    static Iter<Token> createN(String s)
    {
        PeekReader pr = PeekReader.readString(s) ;
        TokenizerText tt = new TokenizerText(pr) ;
        List<Token> x = new ArrayList<>() ;
        while(tt.hasNext())
            x.add(tt.next()) ;
        return Iter.iter(x) ;
    }
    
    public long getColumn()
    {
        return column ;
    }

    public long getLine()
    {
        return line ;
    }

    Token(String string) { this(STRING, string) ; } 
    
    Token(TokenType type) { this(type, null, null) ; }
  
    Token(TokenType type, String image1) { this(type, image1, null) ; }
  
    Token(TokenType type, String image1, String image2)
    { 
      this() ;
      setType(type) ;
      setImage(image1) ;
      setImage2(image2) ;
    }
    
    
//    private Token(TokenType type) { this(type, null, null, null) ; }
//    
//    private Token(TokenType type, String image1) { this(type, image1, null, null) ; }
//    
//    private Token(TokenType type, String image1, String image2)
//    { this(type, image1, image2, null) ; }
//
//    private Token(TokenType type, String image1, Token subToken)
//    { this(type, image1, null, subToken) ; }
//
//
    private Token(TokenType type, String image1, String image2, Token subToken1, Token subToken2)
    {
        this() ;
        setType(type) ;
        setImage(image1) ;
        setImage2(image2) ;
        setSubToken1(subToken1) ;
        setSubToken2(subToken2) ;
    }
    
    private Token() { this(-1, -1) ; }
    
    public Token(long line, long column) { this.line = line ; this.column = column ; }
    
    public Token(Token token)
    { 
        this(token.tokenType, 
             token.tokenImage, token.tokenImage2,
             token.subToken1, token.subToken2) ;
        this.cntrlCode      = token.cntrlCode ;
        this.line           = token.line ; 
        this.column         = token.column ;
    }
    
    // Convenience operations for accessing tokens. 
    
    public String asString() {
        switch (tokenType)
        {
            case STRING: 
            case STRING1: case STRING2: 
            case LONG_STRING1: case LONG_STRING2:
                return getImage() ;
            default:
                return null ;
        }
    }
    
    public int asInt() {
        if ( ! hasType(TokenType.INTEGER) ) return -1 ;
        return Integer.valueOf(tokenImage);
    }
    
    public long asLong()
    {
        return asLong(-1) ;
    }
    
    public long asLong(long dft)
    {
        switch (tokenType)
        {
            case INTEGER:   return Long.valueOf(tokenImage) ;
            case HEX:       return Long.valueOf(tokenImage, 16) ;
            default:
                 return dft ;
        }
    }
    
    public String asWord()
    {
        if ( ! hasType(TokenType.KEYWORD) ) return null ;
        return tokenImage ; 
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
     
    static final String delim1 = "" ;
    static final String delim2 = "" ;
    public String toString(boolean addLocation)
    {
        StringBuilder sb = new StringBuilder() ;
        if ( addLocation && getLine() >= 0 && getColumn() >= 0 )
            sb.append(String.format("[%d,%d]", getLine(), getColumn())) ;
        sb.append("[") ;
        if ( getType() == null )
            sb.append("null") ;
        else
            sb.append(getType().toString()) ;
        
        if ( getImage() != null )
        {
            sb.append(":") ;
            sb.append(delim1) ;
            sb.append(getImage()) ;
            sb.append(delim1) ;
        }
            
        if ( getImage2() != null )
        {
            sb.append(":") ;
            sb.append(delim2) ;
            sb.append(getImage2()) ;
            sb.append(delim2) ;
        }
        
        if ( getSubToken1() != null )
        {
            sb.append(";") ;
            sb.append(delim2) ;
            sb.append(getSubToken1().toString()) ;
            sb.append(delim2) ;
        }   

        if ( getSubToken2() != null )
        {
            sb.append(";") ;
            sb.append(delim2) ;
            sb.append(getSubToken2().toString()) ;
            sb.append(delim2) ;
        }   

        if ( getCntrlCode() != 0 )
        {
            sb.append(":") ; 
            sb.append(getCntrlCode()) ;
        }
        sb.append("]") ;
        return sb.toString() ;
    }
    
    public boolean isEOF() { return tokenType == TokenType.EOF ; }
    
    public boolean isCtlCode() { return tokenType == TokenType.CNTRL ; }

    public boolean isWord() { return tokenType == TokenType.KEYWORD ; }

    public boolean isString()
    {
        switch(tokenType)
        {
            case STRING:
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
                return true ;
            default:
                return false ;
        }
    }

    public boolean isNumber()
    {
        switch(tokenType)
        {
            case DECIMAL: 
            case DOUBLE:
            case INTEGER:
                return true ;
            default:
                return false ;
        }
    }
    
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
            case STRING:
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
                return true ;
            case KEYWORD:
                if ( tokenImage.equals(ImageANY) )
                    return true ;
                return false ;
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
            case STRING:
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
                return true ;
            default:
                return false ;
        }
    }
    
    public boolean isInteger()
    {
        return tokenType.equals(TokenType.INTEGER) ;
    }
    
    public boolean isIRI()
    {
        return tokenType.equals(TokenType.IRI) || tokenType.equals(TokenType.PREFIXED_NAME);
    }

    public boolean isBNode()
    {
        return tokenType.equals(TokenType.BNODE) ;
    }

    
    /** Token to Node, a very direct form that is purely driven off the token.
     *  Turtle and N-triples need to process the token and not call this:
     *  1/ Use bNode label as given
     *  2/ No prefix or URI resolution.
     *  3/ No checking.
     */
    public Node asNode()
    {
        return asNode(null) ;
    }
    
    /** Token to Node, with a prefix map
     *  Turtle and N-triples need to process the token and not call this:
     *  1/ Use bNode label as given
     *  2/ No prefix or URI resolution.
     *  3/ No checking.
     */
    public Node asNode(PrefixMap pmap)
    {
        switch(tokenType)
        {
            // Assumes that bnode labels have been sorted out already.
            case BNODE : return NodeFactory.createAnon(new AnonId(tokenImage)) ;
            case IRI :
                // RiotLib.createIRIorBNode(tokenImage) includes processing <_:label>
                return NodeFactory.createURI(tokenImage) ; 
            case PREFIXED_NAME :
                if ( pmap == null )
                    return NodeFactory.createURI("urn:prefixed-name:"+tokenImage+":"+tokenImage2) ;
                String x = pmap.expand(tokenImage, tokenImage2) ;
                if ( x == null )
                    throw new RiotException("Can't expand prefixed name: "+this) ;
                return NodeFactory.createURI(x) ;
            case DECIMAL :  return NodeFactory.createLiteral(tokenImage, null, XSDDatatype.XSDdecimal)  ; 
            case DOUBLE :   return NodeFactory.createLiteral(tokenImage, null, XSDDatatype.XSDdouble)  ;
            case INTEGER:   return NodeFactory.createLiteral(tokenImage, null, XSDDatatype.XSDinteger) ;
            case LITERAL_DT :
            {
                Token lexToken = getSubToken1() ;
                Token dtToken  = getSubToken2() ;
                
                if ( pmap == null && dtToken.hasType(TokenType.PREFIXED_NAME) )
                    // Must be able to resolve the datattype else we can't find it's datatype.
                    throw new RiotException("Invalid token: "+this) ;
                Node n = dtToken.asNode(pmap);
                if ( ! n.isURI() )
                    throw new RiotException("Invalid token: "+this) ;
                RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(n.getURI()) ;
                return NodeFactory.createLiteral(lexToken.getImage(), null, dt)  ;
            }
            case LITERAL_LANG : return NodeFactory.createLiteral(tokenImage, tokenImage2, null)  ;
            case STRING:
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
                return NodeFactory.createLiteral(tokenImage) ;
            case VAR:
                return Var.alloc(tokenImage) ;
            case KEYWORD:
                if ( tokenImage.equals(ImageANY) )
                    return NodeConst.nodeANY ;
                if ( tokenImage.equals(ImageTrue) )
                    return NodeConst.nodeTrue ;
                if ( tokenImage.equals(ImageFalse) )
                    return NodeConst.nodeFalse ;
                //$FALL-THROUGH$
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
        return hashCodeObject(tokenType) ^
                hashCodeObject(tokenImage) ^
                hashCodeObject(tokenImage2) ^
                hashCodeObject(cntrlCode) ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof Token ) ) return false ;
        Token t = (Token)other ;
        return  equal(tokenType, t.tokenType) &&
                equal(tokenImage, t.tokenImage) &&
                equal(tokenImage2, t.tokenImage2) &&
                equal(cntrlCode, t.cntrlCode) ;
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
    
    public static Token tokenForInteger(long value)
    {
        return new Token(TokenType.INTEGER, Long.toString(value)) ;
    }
    
    public static Token tokenForWord(String word)
    {
        return new Token(TokenType.KEYWORD, word) ; 
    }

    public static Token tokenForNode(Node n)
    {
        return tokenForNode(n, null, null) ;
    }

    public static Token tokenForNode(Node n, Prologue prologue)
    {
        return tokenForNode(n, prologue.getBaseURI(), prologue.getPrefixMap()) ;
    }

    public static Token tokenForNode(Node node, String base, PrefixMap mapping)
    {
            if ( node.isURI() )
            {
                String uri = node.getURI();
                if ( mapping != null )
                {
                    Pair<String,String> pname = mapping.abbrev(uri) ;
                    if ( pname != null )
                        return new Token(TokenType.PREFIXED_NAME, pname.getLeft(), pname.getRight()) ;
                }
                if ( base != null )
                {
                    String x = FmtUtils.abbrevByBase(uri, base) ;
                    if ( x != null ) 
                        return new Token(TokenType.IRI, x) ;
                }
                return new Token(IRI, node.getURI()) ;
            }
            if ( node.isBlank() )
                return new Token(BNODE, node.getBlankNodeLabel()) ;
            if ( node.isVariable() )
                return new Token(VAR, node.getName()) ;
            if ( node.isLiteral() )
            {
                String datatype = node.getLiteralDatatypeURI() ;
                String lang = node.getLiteralLanguage() ;
                String s = node.getLiteralLexicalForm() ;
                
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
                    
                    Node dt = NodeFactory.createURI(datatype) ;
                    Token subToken1 = new Token(STRING, s) ;
                    Token subToken2 = tokenForNode(dt) ;
                    Token t = new Token(LITERAL_DT, s) ;
                    t.setSubToken1(subToken1) ;
                    t.setSubToken2(subToken2) ;
                    return t ;
                }
    
                if ( lang != null && lang.length()>0)
                {
                    Token lex = new Token(s) ;
                    return new Token(LITERAL_LANG, s, lang, lex, null) ;
                }
                
                // Plain.
                return new Token(STRING, s) ; 
            }
            
            if ( node.equals(Node.ANY) )
                return new Token(TokenType.KEYWORD, ImageANY) ;
            
            throw new IllegalArgumentException() ;
            
        }
}
