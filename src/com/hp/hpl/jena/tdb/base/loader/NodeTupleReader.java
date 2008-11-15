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
import java.io.StringReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

import lib.Tuple;

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
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.SyntaxError;
import com.hp.hpl.jena.util.FileUtils;

import event.Event;
import event.EventListener;
import event.EventManager;
import event.EventType;

/** A fast tuple-of-nodes reader (currently, triples only)
 */
public final class NodeTupleReader
{
    //  1 - expect - check if false and react
    //  2 - readURIStr - check one char peek ahead but do any escape
    
    static final int EOF = -1 ;
    static final int NL = '\n' ;
    static final int CR = '\r' ;

    private TupleSink sink ;
    interface TupleSink { void tuple(Tuple<Node> tuple) ; } 
    
    // N-Triples
    static class GraphTupleSink implements TupleSink
    {
        private final Graph graph ;
        GraphTupleSink(Graph g) { this.graph = g ; }
        @Override
        public void tuple(Tuple<Node> tuple)
        {
            if ( tuple.size() != 3 ) 
                throw new lib.InternalError("Tuple not of length 3 for a triple") ;
            Triple t = new Triple(tuple.get(0), tuple.get(1), tuple.get(2)) ;
            graph.add(t) ;
        }
        
    }
    static class NullSink implements TupleSink
    {
        @Override
        public void tuple(Tuple<Node> tuple) {}
    }
    
    private Hashtable<String, Node> anons = new Hashtable<String, Node>();

    private PeekReader in = null;
    private boolean inErr = false;
    private int errCount = 0;
    private static final int sbLength = 200;

    private RDFErrorHandler errorHandler = new RollForwardErrorHandler();

    static final EventType startRead = new EventType("StartRead") ;
    static final EventType finishRead = new EventType("FinishRead") ;
    
    /**
     * Already with ": " at end for error messages.
     */
    private String msgBase; // ???
    static public boolean CheckingNTriples = false ;
    
    static public boolean CheckingIRIs = false ;
    static public boolean KeepParsingAfterError = true ;
    final StringBuilder buffer = new StringBuilder(sbLength);

    public NodeTupleReader(String string)
    {
        this((TupleSink)null, new StringReader(string), "TEST") ;
    }
    
    private NodeTupleReader(final Graph graph, Reader reader, String base)
    {
        this(new GraphTupleSink(graph), reader, base) ;
        CheckingNTriples = true ;
        // Route events to graph events
        event.EventManager.register(sink, startRead, new EventListener(){
            @Override
            public void event(Object dest, Event event)
            {
                graph.getEventManager().notifyEvent( graph , GraphEvents.startRead ) ;
            }}) ;
        event.EventManager.register(sink, finishRead, new EventListener(){
            @Override
            public void event(Object dest, Event event)
            {
                graph.getEventManager().notifyEvent( graph , GraphEvents.finishRead ) ;
            }}) ;
    }

    public NodeTupleReader(TupleSink sink, Reader reader, String base)
    {
        this.sink = sink ;
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
        read(graph, FileUtils.asUTF8(in), base);
    }
    
    public static void read(Graph graph, Reader reader, String base)
    {
        if ( graph == null )
            throw new IllegalArgumentException("Null for graph") ;
        NodeTupleReader b = new NodeTupleReader(graph, reader, base) ;
        b.readRDF();
    }

    private void readRDF()  {
        boolean noCache = false ;
        if ( noCache ) 
            Node.cache(false) ;
        try {
            EventManager.send(sink, new Event(startRead, null)) ;
            process();
        } catch (RuntimeException ex)
        { 
            ex.printStackTrace(System.err) ;
            throw ex ;
        }
        finally {
            EventManager.send(sink, new Event(finishRead, null)) ;
            if ( noCache ) Node.cache(true) ;
        }
        if ( ! KeepParsingAfterError && errCount > 0 )
            throw new SyntaxError("Unknown") ;
    }
    
    private final void process()
    {
        while (!in.eof())                       // Each line.
        {
            inErr = false ;
            readOne() ;
            if ( inErr && ! KeepParsingAfterError )
                return ;
        }
    }
    
    // ----
    Tuple<Node> lastTuple = null ;
    /** Testing */
    Tuple<Node> readTuple()
    {
        readOne() ;
        return lastTuple ;
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
        if ( inErr )
        {
            skipToNewlineLax() ;
            return ;
        }
        
        if ( CheckingNTriples && ! subject.isURI() && !subject.isBlank() )
        {
            syntaxError("Subject is not an IRI or blank node") ;
            subject = null ;
            return ;
        }

        skipWhiteSpace();
        predicate = readNode() ;
        if ( inErr )
        {
            skipToNewlineLax() ;
            return ;
        }
        if ( CheckingNTriples && ! predicate.isURI() )
        {
            syntaxError("Predicate is not an IRI") ;
            predicate = null ;
            return ;
        }

        skipWhiteSpace();
        object = readNode() ;
        if ( inErr )
        {
            skipToNewlineLax() ;
            return ;
        }
        if ( CheckingNTriples && ! object.isURI() && !object.isBlank() && ! object.isLiteral() )
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
            skipToNewlineStrict() ;
            return ;
        }
        
        if ( subject != null && predicate != null && object != null )
        {
            try {
                emit(subject, predicate, object) ;
            } catch (JenaException ex)
            {
                // We no longer know the column
                warning(ex.getMessage(), in.getLineNum(), -1) ;
            }
        }
    }

    private void emit(Node...nodes)
    {
        if ( CheckingNTriples && nodes.length != 3 )
        {
            syntaxError("Not a 3-tuple") ;
            return ;
        }
        
        Tuple<Node> t = new Tuple<Node>(nodes) ;
        lastTuple = t ;
        // Note : this can throw a JenaException for further checking of the triple. 
        if ( sink != null )
            sink.tuple(t) ;
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
    private final
    int readUnicode4Escape() { return readUnicodeEscape(4) ; }
    
    private final
    int readUnicodeEscape(int N)
    {
        int x = 0 ;
        for ( int i = 0 ; i < N ; i++ )
        {
            int d = readHex() ;
            if ( d < 0 )
                return -1 ;
            x = (x<<4)+d ;
        }
        return x ; 
    }
    
    private final
    int readHex()
    {
        int ch = in.readChar() ;
        if ( ch == EOF )
            syntaxError("Not a hexadecimal character (end of file)") ;

        if ( range(ch, '0', '9') )
            return ch-'0' ;
        if ( range(ch, 'a', 'f') )
            return ch-'a'+10 ;
        if ( range(ch, 'A', 'F') )
            return ch-'A'+10 ;
        
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
            {
                ch = readLiteralEscape() ;
                if ( Character.charCount(ch) == 1 )
                {
                    buffer.append((char)ch);
                    continue ;
                }
                // Too big for Basic Multilingual Plane (BMP)
                char[] pair = Character.toChars(ch) ;
                for ( char chSub : pair )
                    buffer.append((char)chSub);
                continue ;
            }
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
                // Be liberal an allow any escape here?
                if ( expect("u") )
                    ch = readUnicode4Escape();
                else
                    ch = '_' ;
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
//                syntaxError(v.getShortMessage()) ;
                if ( v.isError() )
                    syntaxError(v.getShortMessage()) ;
                else
                    warning(v.getShortMessage()) ;
            }
        }
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
            case 'n': return NL ; 
            case 'r': return CR ;
            case 't': return '\t' ;
            case '"': return '"' ;
            case '\\': return '\\' ;
            case 'u':
                return readUnicode4Escape();
            case 'U':
            {
                
                // attempt to read ... 
                int ch8 = readUnicodeEscape(8);
                if ( ch8 > Character.MAX_CODE_POINT )
                {
                    syntaxError(String.format("illegal code point in \\U sequence value: 0x%08X", ch8));
                    return 0 ;
                }
//                if ( ch8 > Character.MAX_VALUE )
//                {
//                    syntaxError(String.format("code point too large for Java in \\U sequence value: 0x%08X", ch8));
//                    return '?' ;
//                }
                
                return ch8 ; 
            }
            default:
                syntaxError(String.format("illegal escape sequence value: %c (0x%02X)", c, c));
                return 0 ;
        }
    }
    
    private void warning(String s) { warning(s, in.getLineNum(), in.getColNum()) ; }
    
    private void warning(String s, int line, int charpos) {
        errorHandler.warning(
            new SyntaxError(
                syntaxErrorMessage(
                    "Warning",
                    s,
                    line, charpos)));
    }

    private void syntaxError(String s) { syntaxError(s, in.getLineNum(), in.getColNum()) ; }
    
    private void syntaxError(String s, int line, int charpos) {
        errCount ++ ;
        errorHandler.error(syntaxException(s, line, charpos)) ;
        inErr = true;
    }
    
    private SyntaxError syntaxException(String s, int lineNum, int charpos)
    {
        return new SyntaxError(
                syntaxErrorMessage(
                    "Syntax error",
                    s,
                    lineNum,
                    charpos));
    }
    
    private String readLang() {
        buffer.setLength(0) ;

        while (true) {
            int inChar = in.peekChar();
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

    private void skipToNewlineStrict()
    {
        // Skip to EOL : (CR | CR NL | NL)
        // Skips the newline.
        while( !in.eof() )
        {
            int ch = in.readChar() ;
            if ( ch == NL )
                break ;
            if ( ch == CR )
            {
                // Windows line ending.
                if ( in.peekChar() == NL )
                    in.readChar() ;
                break ;
            }
        }
    }
    
    private void skipToNewlineLax()
    {
        skipToNewlineStrict() ;
        skipCurrentNewlineChars() ;
    }
    
    private void skipCurrentNewlineChars()
    {
        // Skip multiple EOL characters. 
        while ( !in.eof() )
        {
            int ch = in.peekChar() ;
            if (ch != NL || ch != CR) 
                break ;
            in.readChar() ;
        }
    }
    
    private void comment()
    {
        while ( !in.eof() )
        {
            int ch = in.readChar() ;
            if ( ch == NL )
                return ;
            if ( ch == CR )
            {
                // Windows line ending.
                if ( in.peekChar() == NL )
                    in.readChar() ;
                return ;
            }
        }
    }

    private String syntaxErrorMessage(String sort, String msg, 
                                      int linepos, int charpos)
    {
        StringBuilder x = new StringBuilder() ; 
        x.append(msgBase) ;
        if ( sort != null )
        {
            x.append(sort) ;
            x.append(" at line ") ;
        }
        else
            x.append("Line ") ;
        x.append(Integer.toString(linepos)) ;
        if ( charpos >= 0 )
        {
            x.append(" position ") ;
            x.append(Integer.toString(charpos)) ;
        }
        x.append(": ") ;
        x.append(msg) ;
        return x.toString() ;
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