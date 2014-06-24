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

package com.hp.hpl.jena.sparql.sse;

import java.io.OutputStream ;
import java.util.Map ;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

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
        
        @Override
        public void visit(Item item, Node node)
        { out.print(FmtUtils.stringForNode(node, sCxt)) ; }
        
        @Override
        public void visit(Item item, String symbol)
        { out.print(symbol) ; } 

        @Override
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
 
        @Override
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

            for ( String s : map.keySet() )
            {
                if ( !first )
                {
                    out.println();
                }
                first = false;
                String k = s;
                String v = map.get( k );

                out.print( "(" );
                out.print( k );
                out.print( ':' );
                // Include at least one space 
                out.print( ' ', 6 - k.length() );
                out.print( FmtUtils.stringForURI( v ) );
                out.print( ")" );
            }
            out.decIndent(2) ;
            out.print(")") ;
        }
    }
}
