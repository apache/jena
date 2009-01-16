/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class ItemWriter
{
    public static boolean includeBase = false ; 
    private static boolean CloseSameLine = true ;
    
    public static void write(IndentedWriter out, Item item, SerializationContext sCxt)
    {
        Print pv = new Print(out, sCxt) ;
        pv.startPrint() ;
        item.visit(pv) ;
        pv.finishPrint() ;
    }
    
    public static void write(OutputStream out, Item item)
    {
        IndentedWriter iw = new IndentedWriter(out) ;
        write(iw, item , null) ;
        iw.ensureStartOfLine() ;
        iw.flush();
    }
    
    private static class Print implements ItemVisitor
    {
        IndentedWriter out ;
        SerializationContext sCxt ;
        boolean doneBase = false ;
        boolean donePrefix = false ;
        
        Print(IndentedWriter out, SerializationContext sCxt)
        {
            if ( sCxt == null )
                sCxt = new SerializationContext() ;
            this.out = out ; 
            this.sCxt = sCxt ;
        }
        
        void startPrint()
        {
            if ( sCxt != null )
            {
                if ( includeBase && sCxt.getBaseIRI() != null )
                {
                    out.print("(base ") ;
                    out.println(FmtUtils.stringForURI(sCxt.getBaseIRI())) ;
                    doneBase = true ;
                    out.incIndent() ;
                }
                PrefixMapping pmap = sCxt.getPrefixMapping() ;
                if ( pmap != null )
                {
                    Map<String,String> pm = pmap.getNsPrefixMap() ;
                    donePrefix = ( pm.size() != 0 ) ;
                    if ( pm.size() != 0 )
                    {
                        out.println("(prefix") ;
                        out.incIndent() ;
                        printPrefixes(pm, out) ;
                        out.println();
                    }
                }
            }
        }

        void finishPrint()
        {
            if ( doneBase )
            {
                out.print(")");
                out.decIndent() ;
            }
            if ( donePrefix )
            {
                out.print(")");
                out.decIndent() ;
            }
        }
        
        public void visit(Item item, Node node)
        { out.print(FmtUtils.stringForNode(node, sCxt)) ; }
        
        public void visit(Item item, String symbol)
        { out.print(symbol) ; } 

        public void visit(Item item, ItemList list)
        {
            out.print("(") ;
            
            boolean listMode = false ;
            for ( Item subItem : list )
            {
                if ( subItem.isList() )
                {
                    listMode = true ;
                    break ;
                }
            }
            
            // Lists are printed with structure.
            // If no lists, print on one line.
            if ( listMode )
                printAsList(list) ;
            else
                printOneLine(list) ;
        }
 
        public void visitNil()
        { out.print("nil") ; }

        private void printAsList(ItemList list)
        {
            boolean first = true ; 
            int indentlevel = out.getUnitIndent() ;
            if ( list.size() >= 1 && list.get(0).isList() )
                indentlevel = 1 ;
            
            for ( Item subItem : list )
            {
                if ( ! first ) 
                    out.println() ;
                subItem.visit(this) ;
                if ( first )
                    out.incIndent(indentlevel) ;
                first = false ;
            }
            
            if ( ! first )
                out.decIndent(indentlevel) ;
            if ( ! CloseSameLine )
                out.println();
            out.print(")") ;
        }
        
        private void printOneLine(ItemList list)
        {
            boolean first = true ;

            for ( Item subItem : list )
            {
                if ( ! first )
                    out.print(" ") ;
                first = false ;
                subItem.visit(this) ;
                
            }
            out.print(")") ;
        }

        private void printPrefixes(Map<String, String> map, IndentedWriter out)
        {
            if ( map.size() == 0 )
                return ;
            
            out.print("( ") ;
            out.incIndent(2) ;
            
            boolean first = true ;
            
            for ( Iterator<String> iter = map.keySet().iterator() ; iter.hasNext() ; )
            {
                if ( ! first )
                    out.println();
                first = false ;
                String k = iter.next() ;
                String v = map.get(k) ;
                
                out.print("(") ;
                out.print(k) ;
                out.print(':') ;
                // Include at least one space 
                out.print(' ', 6-k.length()) ;
                out.print(FmtUtils.stringForURI(v)) ;
                out.print(")") ;
            }
            out.decIndent(2) ;
            out.print(")") ;
        }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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