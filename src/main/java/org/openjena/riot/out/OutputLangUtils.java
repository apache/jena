/**
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

package org.openjena.riot.out;

import java.io.IOException ;
import java.io.Writer ;
import java.net.MalformedURLException ;

import org.openjena.atlas.io.OutputUtils ;
import org.openjena.riot.system.PrefixMap ;
import org.openjena.riot.system.Prologue ;
import org.openjena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.IRIRelativize ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Utilites for formatter output */
public class OutputLangUtils
{
    
    /* This will become the way to output RDF terms and structures.
     *   
     */
    
    // "Prologue" is an input concept - whats the equivalent for output?
    //   Prefix mapping + base URI abbreviation.
    
    // TODO Use NodeFmtLib (objectified) in output(Writer, node,*)
    
    // Make an object so it can have per-instance flags
    // ASCII vs UTF-8
    // Abbreviate numbers or not.
    // Avoids creating intermediate strings.
    // == Class with two subclasses. Turtle policy and N-triples policy.
    
    private static boolean asciiOnly = true ;

    static public void output(Writer out, Quad quad, Prologue prologue, NodeToLabel labelPolicy)
    {
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        Node g = quad.getGraph() ;
        output(out, s, p, o, g, prologue, labelPolicy) ;
    }
    
    // See also SinkQuadWriter and deduplicate
    static public void output(Writer out, Node s, Node p, Node o, Node g, Prologue prologue, NodeToLabel labelPolicy)
    {
        output(out, s, prologue, labelPolicy) ;
        print(out," ") ;
        output(out, p, prologue, labelPolicy) ;
        print(out," ") ;
        output(out, o, prologue, labelPolicy) ;
        if ( g != null )
        {
            print(out," ") ;
            output(out, g, prologue, labelPolicy) ;
        }
        print(out," .") ;
        println(out) ;
    }
    
    // See also SinkTripleWriter and deduplicate
    static public void output(Writer out, Triple triple, Node graphNode, Prologue prologue, NodeToLabel labelPolicy)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        output(out, s, p, o, graphNode, prologue, labelPolicy) ;
    }
    
    static public void output(Writer out, Triple triple, Prologue prologue, NodeToLabel labelPolicy)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        output(out, s, p, o, null, prologue, labelPolicy) ;
    }
    
    /** Use with caution - better to pass in a Node to Label mapper */
    static public void output(Writer out, Node node, Prologue prologue)
    {
        output(out, node, prologue, SyntaxLabels.createNodeToLabel()) ;
    }
    
    static public void output(Writer out, Node node, Prologue prologue, NodeToLabel labelPolicy)
    {
        // NodeVisitor would be nice but don't want to create an object per static call. 
        
        if ( node.isURI() ) 
        { 
            printIRI(out, node.getURI(), prologue) ;
            return ; 
        }
        if ( node.isBlank() )
        {
            if ( labelPolicy == null )
                // This is encoded to be safe.
                labelPolicy = NodeToLabel.labelByInternal() ;
            
//            String label = node.getBlankNodeLabel() ;
//            label = NodeFmtLib.safeBNodeLabel(label) ;
            
            // Assumes single scope.
            String label = labelPolicy.get(null, node) ;
            print(out,label) ;
            return ;
        }
        
        if ( node.isLiteral() )
        {
            printLiteral(out, node, prologue) ;
            return ;
        }

        if ( node.isVariable() )
        {
            print(out,'?') ;
            print(out, node.getName()) ;
            return ; 
        }
        
        if ( node.equals(Node.ANY) )
        {
        	print(out, "ANY") ;
        	return ;
        }
        
        System.err.println("Illegal node: "+node) ;
    }

    // TODO Do Turtle number abbreviations, controlled by a flag.
    // So there are flags ==> make an object
    private static void printLiteral(Writer out, Node node, Prologue prologue)
    {
        print(out,'"') ;
        outputEsc(out, node.getLiteralLexicalForm(), true) ;
        print(out,'"') ;

        if ( node.getLiteralLanguage() != null && node.getLiteralLanguage().length()>0)
        {
            print(out,'@') ;
            print(out,node.getLiteralLanguage()) ;
        }

        if ( node.getLiteralDatatypeURI() != null )
        {
            print(out,"^^") ;
            printIRI(out,node.getLiteralDatatypeURI(), prologue) ;
        }
        return ; 
    }

    
    private static void printIRI(Writer out, String iriStr, Prologue prologue)
    {
        if ( prologue != null )
        {
            PrefixMap pmap = prologue.getPrefixMap() ;
            if (  pmap != null )
            {
                String pname = prefixFor(iriStr, pmap) ;
                if ( pname != null )
                {
                    print(out,pname) ;
                    return ;
                }
            }
            String base = prologue.getBaseURI() ; 
            if ( base != null )
            {
                String x = abbrevByBase(iriStr, base) ;
                if ( x != null )
                    iriStr = x ;
                // And drop through.
            }
        }
        
        print(out,"<") ;
        // IRIs can have non-ASCII characters.
        if ( asciiOnly )
            outputEsc(out, iriStr, false) ;
        else
            print(out,iriStr) ;
        print(out,">") ;
    }

    private static String prefixFor(String uri, PrefixMap mapping)
    {
        if ( mapping == null ) return null ;

        String pname = mapping.abbreviate(uri) ;
        if ( pname != null ) // Assume only validperfixes in the map else ... && checkValidPrefixName(pname) )
            return pname ;
        return null ;
    }

    static private int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.CHILD ;
    static public String abbrevByBase(String uri, String base)
    {
        if ( base == null )
            return null ;
        IRI baseIRI = IRIFactory.jenaImplementation().construct(base) ;
        IRI rel = baseIRI.relativize(uri, relFlags) ;
        String r = null ;
        try { r = rel.toASCIIString() ; }
        catch (MalformedURLException  ex) { r = rel.toString() ; }
        return r ;
    }
    
  

    private static void print(Writer out, String s)
    {
        try { out.append(s) ; } catch (IOException ex) {}
    }

    private static void print(Writer out, char ch)
    {
        try { out.append(ch) ; } catch (IOException ex) {}
    }

    private static void println(Writer out)
    {
        try { out.append("\n") ; } catch (IOException ex) {}
    }

    static final boolean applyUnicodeEscapes = true ;
    
    /** Output a string, using \t etc and  \ u escape mechanisms.
     * @param out   Writer for output
     * @param s     String to process
     * @param useSlashEscapes   Whether to use \t etc (\\ is awlays possible).
     *    
     */
    @Deprecated
    static public void outputEsc(Writer out, String s, boolean useSlashEscapes)
    {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            
            // \\ Escape always possible.
            if (c == '\\') 
            {
                print(out,'\\') ;
                print(out,c) ;
                continue ;
            }
            if ( useSlashEscapes )
            {
                if ( c == '"' )         { print(out,"\\\""); continue ; }
                else if ( c == '\n')    { print(out,"\\n");  continue ; }
                else if (c == '\t')     { print(out,"\\t"); continue ; }
                else if (c == '\r')     { print(out,"\\r"); continue ; }
                else if (c == '\f')     { print(out,"\\f"); continue ; }
            }
            // Not \-style esacpe. 
            if ( c >= 32 && c < 127 )
                print(out,c);
            else if ( !asciiOnly )
                print(out,c);
            else
            {
                // Outside the charset range.
                // Does not cover beyond 16 bits codepoints directly
                // (i.e. \U escapes) but Java keeps these as surrogate
                // pairs and will print as characters
                print(out, "\\u") ;
                OutputUtils.printHex(out, c, 4) ;
            }
        }
    }
}
