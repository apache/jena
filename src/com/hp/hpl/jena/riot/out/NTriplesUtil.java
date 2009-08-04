/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.out;

import java.io.PrintStream;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class NTriplesUtil
{
    // TODO - Chnage to an OutputStream -- because we only want ASCII
    static public void triple(PrintStream out, Triple triple)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
//        if ( ! ( s.isURI() || s.isBlank() ) )
//            throw new TurtleParseException("["+line+", "+col+"] : Error: Subject is not a URI or blank node") ;
//        if ( ! p.isURI() )
//            throw new TurtleParseException("["+line+", "+col+"] : Error: Predicate is not a URI") ;
//        if ( ! ( o.isURI() || o.isBlank() || o.isLiteral() ) ) 
//            throw new TurtleParseException("["+line+", "+col+"] : Error: Object is not a URI, blank node or literal") ;
      
        print(out, s) ;
        print(out," ") ;
        print(out, p) ;
        print(out," ") ;
        print(out, o) ;
        print(out," .") ;
        println(out) ;
        out.flush() ;
        
    }

    static private void print(PrintStream out,Node node)
    {
        if ( node.isURI() ) 
        { 
            print(out,"<") ;
            print(out,node.getURI()) ;
            print(out,">") ;
            return ; 
        }
        if ( node.isBlank() )
        {
            print(out,"_:") ;
            print(out,node.getBlankNodeLabel()) ;
            return ;
        }
        if ( node.isLiteral() )
        {
            print(out,'"') ;
            outputEsc(out, node.getLiteralLexicalForm()) ;
            print(out,'"') ;

            if ( node.getLiteralLanguage() != null && node.getLiteralLanguage().length()>0)
            {
                print(out,'@') ;
                print(out,node.getLiteralLanguage()) ;
            }

            if ( node.getLiteralDatatypeURI() != null )
            {
                print(out,"^^<") ;
                print(out,node.getLiteralDatatypeURI()) ;
                print(out,">") ;
            }
            return ; 
        }
        System.err.println("Illegal node: "+node) ;
    }
    
    private static void print(PrintStream out, String s)
    {
        out.print(s) ;
    }

    private static void print(PrintStream out, char ch)
    {
        out.print(ch) ;
    }

    private static void println(PrintStream out)
    {
        out.println() ;
    }

    
    static boolean applyUnicodeEscapes = true ;
    
//    static private void writeString(String s, PrintWriter writer) {
//
//        for (int i = 0; i < s.length(); i++) {
//            char c = s.charAt(i);
//            if (c == '\\' || c == '"') {
//                writer.print('\\');
//                writer.print(c);
//            } else if (c == '\n') {
//                writer.print("\\n");
//            } else if (c == '\r') {
//                writer.print("\\r");
//            } else if (c == '\t') {
//                writer.print("\\t");
//            } else if (c >= 32 && c < 127) {
//                writer.print(c);
//            } else {
//                String hexstr = Integer.toHexString(c).toUpperCase();
//                int pad = 4 - hexstr.length();
//                writer.print("\\u");
//                for (; pad > 0; pad--)
//                    writer.print("0");
//                writer.print(hexstr);
//            }
//        }
//    }
    
    
    static public void outputEsc(PrintStream out, String s)
    {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            
            // Escape escapes and quotes
            if (c == '\\' || c == '"' ) 
            {
                print(out,'\\') ;
                print(out,c) ;
            }
            else if (c == '\n') print(out,"\\n");
            else if (c == '\t') print(out,"\\t");
            else if (c == '\r') print(out,"\\r");
            else if (c == '\f') print(out,"\\f");
            else if ( c >= 32 && c < 127 )
                print(out,c);
            else
            {
                // TODO Avoid string costs.
                // Unsubtle.  Does not cover beyond 16 bits codepoints directly but 
                // Java keeps these as surrogate pairs and will print as two \ u escapes. 
                String hexstr = Integer.toHexString(c).toUpperCase();
                int pad = 4 - hexstr.length();
                print(out,"\\u");
                for (; pad > 0; pad--)
                    print(out,"0");
                print(out,hexstr);
            }
        }
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