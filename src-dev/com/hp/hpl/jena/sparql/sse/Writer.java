/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.io.PrintStream;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;


public class Writer
{
    private static boolean CloseSameLine = true ;
    
    public static void write(IndentedWriter out, Item item, SerializationContext sCxt)
    {
        item.visit(new Print(out, sCxt)) ;
    }
    
    public static void write(PrintStream out, Item item)
    {
        IndentedWriter iw = new IndentedWriter(out) ;
        item.output(iw) ;
        iw.ensureStartOfLine() ;
        iw.flush();
    }
    
    private static class Print implements ItemVisitor
    {
        IndentedWriter out ;
        SerializationContext sCxt ;
        Print(IndentedWriter out, SerializationContext sCxt)
        {
            if ( sCxt == null )
                sCxt = new SerializationContext() ;
            this.out = out ; 
            this.sCxt = sCxt ;
        }

        public void visit(Item item, Node node)
        { out.print(FmtUtils.stringForNode(node, sCxt)) ; }
        
        public void visit(Item item, String word)
        { out.print(word) ; } 

        public void visit(Item item, ItemList list)
        {
            out.print("(") ;
            
            boolean listMode = false ;
            for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
            {
                Item subItem = (Item)iter.next() ;
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
            
            for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
            {
                Item subItem = (Item)iter.next() ;
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

            for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
            {
                Item subItem = (Item)iter.next() ;
                if ( ! first )
                    out.print(" ") ;
                first = false ;
                subItem.visit(this) ;
                
            }
            out.print(")") ;
        }

    }
    
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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