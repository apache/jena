/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.turtle;

import java.io.PrintStream;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/** Emit N-triples */
public class Turtle2NTriples implements TurtleEventHandler
{
    PrintStream out = System.out ;
    public Turtle2NTriples(PrintStream out) { this.out = out ; }
    
    public void triple(int line, int col, Triple triple)
    {
        //Check it's valid triple.
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
        if ( ! ( s.isURI() || s.isBlank() ) )
            throw new TurtleParseException("["+line+", "+col+"] : Error: Subject is not a URI or blank node") ;
        if ( ! p.isURI() )
            throw new TurtleParseException("["+line+", "+col+"] : Error: Predicate is not a URI") ;
        if ( ! ( o.isURI() || o.isBlank() || o.isLiteral() ) ) 
            throw new TurtleParseException("["+line+", "+col+"] : Error: Object is not a URI, blank node or literal") ;
      
        outputNode(s) ;
        out.print(" ") ;
        outputNode(p) ;
        out.print(" ") ;
        outputNode(o) ;
        out.print(" .") ;
        out.println() ;
        out.flush() ;
        
    }

    private void outputNode(Node node)
    {
        if ( node.isURI() ) 
        { 
            out.print("<") ;
            out.print(node.getURI()) ;
            out.print(">") ;
            return ; 
        }
        if ( node.isBlank() )
        {
            out.print("_:") ;
            out.print(node.getBlankNodeLabel()) ;
            return ;
        }
        if ( node.isLiteral() )
        {
            out.print('"') ;
            outputEsc(node.getLiteralLexicalForm(), true) ;
            out.print('"') ;

            if ( node.getLiteralLanguage() != null && node.getLiteralLanguage().length()>0)
            {
                out.print('@') ;
                out.print(node.getLiteralLanguage()) ;
            }

            if ( node.getLiteralDatatypeURI() != null )
            {
                out.print("^^<") ;
                out.print(node.getLiteralDatatypeURI()) ;
                out.print(">") ;
            }
            return ; 
        }
        System.err.println("Illegal node: "+node) ;
    }

    public void startFormula(int line, int col)
    { throw new TurtleParseException("["+line+", "+col+"] : Error: Formula found") ; }

    public void endFormula(int line, int col)
    { throw new TurtleParseException("["+line+", "+col+"] : Error: Formula found") ; }

    public void prefix(int line, int col, String prefix, String iri)
    {  }
    
    static boolean applyUnicodeEscapes = true ;
    
    public  void outputEsc(String s, boolean singleLineString)
    {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            // Escape escapes and quotes
            if (c == '\\' || c == '"' )
            {
                out.print('\\') ;
                out.print(c) ;
                continue ;
            }
            
            // Characters to literally output.
            // This would generate 7-bit safe files 
//            if (c >= 32 && c < 127)
//            {
//                sbuff.append(c) ;
//                continue;
//            }    

            // Whitespace
            if ( singleLineString && ( c == '\n' || c == '\r' || c == '\f' ) )
            {
                if (c == '\n') out.print("\\n");
                if (c == '\t') out.print("\\t");
                if (c == '\r') out.print("\\r");
                if (c == '\f') out.print("\\f");
                continue ;
            }
            
            // Output as is (subject to UTF-8 encoding on output that is)
            
            if ( ! applyUnicodeEscapes )
                out.print(c) ;
            else
            {
                // Unicode escapes
                // ASCII
                // c < 32, c >= 127, not whitespace or other specials
                if ( c >= 32 && c < 127 )
                {
                    out.print(c) ;
                }
                else
                {
                    // Unsubtle.
                    String hexstr = Integer.toHexString(c).toUpperCase();
                    int pad = 4 - hexstr.length();
                    out.print("\\u");
                    for (; pad > 0; pad--)
                        out.print("0");
                    out.print(hexstr);
                }
            }
        }
    }
 

}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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