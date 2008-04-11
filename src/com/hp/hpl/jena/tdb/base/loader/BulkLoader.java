/*
 *  (c) Copyright 2001, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 *
 * $Id: NTripleReader.java,v 1.16 2007/01/02 11:48:30 andy_seaborne Exp $
 */

package com.hp.hpl.jena.tdb.base.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.SyntaxError;
import com.hp.hpl.jena.util.FileUtils;

/** BulkLoader
 *  (also a fast N-triples reader)
 */
public final class BulkLoader
{
    static final Log log = LogFactory.getLog(BulkLoader.class);
    static final int EOF = -1 ;

    //private Model model = null;
    private Graph graph ; 
    private Hashtable<String, Node> anons = new Hashtable<String, Node>();

    private PeekReader in = null;
    private boolean inErr = false;
    private int errCount = 0;
    private static final int sbLength = 200;

    private RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();

    /**
     * Already with ": " at end for error messages.
     */
    private String msgBase;
    boolean KeepParsingAfterError = false ;
    boolean CheckingRDF = true ;
    boolean CheckingIRIs = false ;
    final StringBuilder buffer = new StringBuilder(sbLength);

    
    public BulkLoader()
    {
        
    }
    
    
    // Testing ONLY
    BulkLoader(Reader r)
    {
        this(null, r, "TEST") ;
    }
    
    private BulkLoader(Graph graph, Reader reader, String base)
    {
      this.graph = graph ;
      this.msgBase = ( base == null ? "" : (base + ": ") );
      this.in = PeekReader.make(reader);
    }

    // XXX Forces use of file:
    public static void read(Graph graph, String url)  {
        try {
            read(
                graph,
                new InputStreamReader(((new URL(url))).openStream()),
                url);
        } catch (Exception e) {
            throw new JenaException(e);
        }
    }
    
    public static void read(Graph graph, InputStream in, String base)
         {
        // N-Triples must be in ASCII, we permit UTF-8.
        read(graph, FileUtils.asBufferedUTF8(in), base);
    }
    
    public static void read(Graph graph, Reader reader, String base)
    {
        if ( graph == null )
            throw new IllegalArgumentException("Null for graph") ;
        BulkLoader b = new BulkLoader(graph, reader, base) ;
        b.readRDF();
    }

    private void readRDF()  {
        boolean noCache = false ;
        if ( noCache ) 
            Node.cache(false) ;
        try {
            graph.getEventManager().notifyEvent( graph , GraphEvents.startRead ) ;
            unwrappedReadRDF();
        } finally {
            graph.getEventManager().notifyEvent( graph , GraphEvents.finishRead ) ;
            if ( noCache ) Node.cache(true) ;
        }
        if ( errCount > 0 )
            throw new SyntaxError("Unknown") ;
    }
    
    private final void unwrappedReadRDF()
    {
        while (!in.eof())                       // Each line.
        {
            readOne() ;
            if ( inErr && ! KeepParsingAfterError )
                return ;
        }
    }

    Triple emittedTriple = null ;
    
    Triple readTriple()
    {
        readOne() ;
        return emittedTriple ;
    }
    
    void readOne()
    {
        Node subject = null ;
        Node predicate = null ;
        Node object = null ;
        
        skipWhiteSpace();

        if ( in.eof() )
            return ;

        subject = readNode() ;
        if ( CheckingRDF && ! subject.isURI() && !subject.isBlank() )
        {
            syntaxError("Subject is not an IRI or blank node") ;
            subject = null ;
            return ;
        }

        skipWhiteSpace();
        predicate = readNode() ;
        if ( CheckingRDF && ! predicate.isURI() )
        {
            syntaxError("Predicate is not an IRI") ;
            predicate = null ;
            return ;
        }

        skipWhiteSpace();
        object = readNode() ;
        if ( CheckingRDF && ! object.isURI() && !object.isBlank() && ! object.isLiteral() )
        {
            syntaxError("Object is not an IRI, blank node or literal") ;
            object = null ;
            return ;
        }

        skipWhiteSpace();
        int ch = in.readChar() ;
        if (ch != '.' )
        {
            syntaxError("End of triple not found") ;
            subject = null ;
            predicate = null ;
            object = null ;
            // Skip to EOL
            while( !in.eof() )
            {
                int x = in.readChar() ;
                if ( x == '\n' )
                    break ;
            }
            return ;
        }
        
        if ( subject != null && predicate != null && object != null )
            emit(subject, predicate, object) ;
    }

    void emit(Node subject, Node predicate, Node object)
    {
        emittedTriple = new Triple(subject, predicate, object) ;
        emit(emittedTriple) ;
    }
        
    private void emit(Triple t)
    {
        if ( graph != null )
            graph.add(t) ;
    }

    Node readNode()
    {
        inErr = false ;
        switch (in.peekChar())
        {
            case EOF:
                syntaxError("unexpected input");
                return null;
            case '"' :
                return readLiteral() ;
            case '<' :
                return readURI() ;
            case '_' :
                return readBlank();
            default :
                syntaxError("unexpected input");
                return null;
        }
    }

    // private
    int readUnicode4Escape()
    {
        int x = 0 ;
        for ( int i = 0 ; i < 4 ; i++ )
        {
            int d = readHex() ;
            if ( d < 0 )
                return -1 ;
            x = x<<8+d ;
        }
        return x ; 
    }
    
    // private
    int readHex()
    {
        int ch = in.readChar() ;
        if ( ch == EOF )
        {}

        if ( range(ch, '0', '9') )
            return ch-'0' ;
        if ( range(ch, 'a', 'f') )
            return ch-'a' ;
        if ( range(ch, 'A', 'F') )
            return ch-'A' ;
        
        syntaxError("Not a hexadecimal character: "+(char)ch) ;
        return -1 ; 
    }

    private Node readURI()
    {
        String iri = readIRIStr() ;
        if ( CheckingIRIs )
            checkIRI(iri) ;
        return Node.createURI(iri) ;
    }
    
    private Node readLiteral()  {
        buffer.setLength(0) ;
        in.readChar();      // Skip opening " 
        
        while (true) {
            if (badEOF())
                return null;

            int ch = in.readChar();
            //char c = (char)ch ;
            if (ch == '\\') 
                ch = readLiteralEscape() ;
            else if (ch == '"')
            {
                // End of lexical form.
                String lex = buffer.toString() ; 
                String lang = "" ;
    
                if ('@' == in.peekChar())
                {
                   in.readChar() ;
                   lang = readLang();
                }
    
                RDFDatatype dt = null ;
                if ('^' == in.peekChar())
                {
                    expect("^^") ;
                    if ( in.peekChar() != '<' )
                    {
                        syntaxError("Datatype IRI expected") ;
                        return null ;
                    }
                    String datatypeURI = readIRIStr();
    
                    if ( ! lang.isEmpty() ) 
    				   syntaxError("Language tags are not permitted on typed literals.");
    
    				dt = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
                }
                
                if ( dt == null )
                    return Node.createLiteral(lex, lang, null);
                else
                    return Node.createLiteral(lex, null, dt) ;
            }
            // Still in lexical form  
            
            buffer.append((char)ch);
        }
    }

    private Node readBlank()
    {
        expect("_:") ;
        buffer.setLength(0) ;
        while (true) 
        {
            int ch = in.peekChar() ;
            if ( in.eof() || Character.isWhitespace(ch) )
                break ;
            in.readChar();
            buffer.append((char)ch) ;
        }
    
        String label = buffer.toString() ;
        Node b = anons.get(label);
        if (b == null) {
            b = Node.createAnon(new AnonId(label)) ;
            anons.put(label, b);
        }
        return b;
    }

    private String readIRIStr()
    {
        in.readChar();     // Skip opening <
        buffer.setLength(0) ;
        while(true)
        {
            int ch = in.readChar() ;
            if ( ch == '>' )
                break ;

            if ( ch == '\\' )
            {
                expect("u");
                ch = readUnicode4Escape();
            }
            buffer.append((char)ch);
        }
        return buffer.toString() ;
    }

 
    static IRIFactory iriFactory = IRIFactory.semanticWebImplementation();
    private void checkIRI(String iriStr)
    {
        boolean includeWarnings = true ;
        IRI iri = iriFactory.create(iriStr); // always works
        if (iri.hasViolation(includeWarnings))
        {
            Iterator<?> it = iri.violations(includeWarnings);
            while (it.hasNext()) {
                Violation v = (Violation) it.next();
                syntaxError(v.getShortMessage()) ;
//                if ( v.isError() )
//                    syntaxError(v.getShortMessage()) ;
//                else
//                    warning(v.getShortMessage()) ;
            }
            throw syntaxException(iriStr) ;
        }
    }

    private String readURI2() {
        //final StringBuilder uriBuffer = new StringBuilder(sbLength);
        buffer.setLength(0) ;

        while (in.readChar() != '>') {
            int inChar = in.readChar();

            if (inChar == '\\') {
                expect("u");
                inChar = readUnicode4Escape();
            }
            if (badEOF()) {
                return null;
            }
            buffer.append((char)inChar);
        }
        return buffer.toString();
    }
    
    private boolean range(int ch, char a, char b)
    {
        return ( ch >= a && ch <= b ) ;
    }

    private int readLiteralEscape()
    {
        int c = in.readChar();
        if (in.eof()) {
            inErr = true;
            return 0 ;
        }

        switch (c)
        {
            case 'n': return '\n' ; 
            case 'r': return '\r' ;
            case 't': return '\t' ;
            case '"': return '"' ;
            case '\\': return '\\' ;
            case 'u':
                return readUnicode4Escape();
            default:
                syntaxError("illegal escape sequence '" + c + "'");
                return 0 ;
        }
    }
    

    private void warning(String s) {
        errorHandler.warning(
            new SyntaxError(
                syntaxErrorMessage(
                    "Deprecation warning",
                    s,
                    in.getLineNum(),
                    in.getColNum())));
    }

    private void syntaxError(String s) {
        errCount ++ ;
        errorHandler.error(syntaxException(s)) ;
        inErr = true;
    }
    
    private SyntaxError syntaxException(String s) {
        return new SyntaxError(
                syntaxErrorMessage(
                    "Syntax error",
                    s,
                    in.getLineNum(),
                    in.getColNum()));
    }
    
    private String readLang() {
        buffer.setLength(0) ;

        while (true) {
            int inChar = in.peekChar();
            // XXX Better?
            if ( ! ( range(inChar, 'a', 'z') || range(inChar, 'A', 'Z') || inChar == '-' ) )
                break ; 
            inChar = in.readChar() ;
            buffer.append((char)inChar);
        }
        return buffer.toString();
    }
    
    private boolean badEOF()
    {
        if (in.eof()) {
            inErr = true ;
            syntaxError("premature end of file");
        }
        return inErr;
    }
    
    private boolean expect(String str) {
        for (int i = 0; i < str.length(); i++) {
            char want = str.charAt(i);
            if (badEOF())
                return false;
            int inChar = in.readChar();

            if (inChar != want) {
                //System.err.println("N-triple reader error");
                syntaxError("expected \"" + str + "\"");
                return false;
            }
        }
        return true;
    }

    private void skipWhiteSpace()
    {
        while ( true )
        {
            if (in.eof()) 
                return;

            int ch = in.peekChar() ;
            if ( ! Character.isWhitespace(ch) && ch != '#' )
                return ;

            if (ch == '#')
                comment() ;
            else
                in.readChar() ;
        }
    }

    private void comment()
    {
        while (in.readChar() != '\n') {
            if (in.eof()) {
                return;
            }
        }
    }

    private String syntaxErrorMessage(
        String sort,
        String msg,
        int linepos,
        int charpos) {
        return msgBase
            + sort
            + " at line "
            + linepos
            + " position "
            + charpos
            + ": "
            + msg;
    }
}    


/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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