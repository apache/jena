/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine;

import java.util.Collection ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.seaborne.dboe.engine.row.RowBuilderBase ;
import org.seaborne.dboe.engine.row.RowListBuilderBase ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderLib ;

public class QJT // Quack Join Test
{
    public static <X> boolean equal(List<Row<X>> rowList1, List<Row<X>> rowList2) {
        List<Row<X>> rows1 = rowList1 ; //Iter.toList(rowList1.iterator()) ;
        List<Row<X>> rows2 = Iter.toList(rowList2.iterator()) ; //Mutated

        if ( rows1.size() != rows2.size() )
            return false ;
        
        for ( int i = 0; i < rows1.size(); i++) {
            Row<X> r1 = rows1.get(i) ;
            for ( int j = 0; j < rows2.size(); j++) {
                Row<X> r2 = rows2.get(j) ;
                if ( equal(r1, r2) ) {
                    //rows1.remove(i) ;
                    rows2.remove(j) ;
                    break ;
                }
            }
        }
        return rows2.size() == 0 ;
    }
     
    public static <X> boolean equal(Row<X> row1, Row<X> row2) {
        if ( row1 == null && row2 == null )
            return true ;
        if ( row1 != null && row2 == null )
            return false ;
        if ( row1 == null && row2 != null )
            return false ;
        
        Collection<Var> vars1 = row1.vars() ;
        Collection<Var> vars2 = row2.vars() ;
        if ( ! vars1.equals(vars2) )
            return false ;
        for ( Var v : vars1 ) {
            X x1 = row1.get(v) ;
            X x2 = row2.get(v) ;
            if ( ! Lib.equal(x1, x2) )
                return false ;
        }
        return true ;
    }
    
    /** Create some test data : a table of (var, integers) */
    public static RowList<Integer> parseTableInt(String... s) {
        return parseTable(item2Int, s) ;
    }

    /** Create some test data : a table of (var, string) */
    public static RowList<String> parseTableString(String... s) {
        return parseTable(item2String, s) ;
    }

    private static <X> RowList<X> parseTable(ItemParser<X> parser, String... s) {        
        RowListBuilder<X> builder = new RowListBuilderBase<>() ;
        
        String x = StrUtils.strjoinNL(s) ;
        Item item = SSE.parse(x) ;

        ItemList list = item.getList() ;
        BuilderLib.checkTag(list, "table") ;
        list = list.cdr() ;

        for (Item e : list) {
            Row<X> r = parseRow(parser, e) ;
            builder.add(r) ;
        }
        return builder.build() ;
    }

    public static JoinKey parseJoinKey(String... s) {
        String x = StrUtils.strjoinNL(s) ;
        Item item = SSE.parse(x) ;
        ItemList list = item.getList() ;
        BuilderLib.checkTag(list, "key") ;
        JoinKey.Builder builder = new JoinKey.Builder() ; 
        list = list.cdr() ;
        for (Item e : list) {
            Var v = Var.alloc(e.getNode()) ;
            builder.add(v) ;
        }
        
        return builder.build() ;
    }
    
    
    /** Create some test data : a row of (var, string) */
    public static Row<Integer> parseRowInt(String... s) {
        return parseRow(item2Int, s) ;
    }

    /** Create some test data : a row of (var, string) */
    public static Row<String> parseRowString(String... s) {
        return parseRow(item2String, s) ;
    }

    private static <X> Row<X> parseRow(ItemParser<X> parser, String... s) {
        String x = StrUtils.strjoinNL(s) ;
        Item item = SSE.parse(x) ;
        return  parseRow(parser, item) ;
    }
    
    private static <X> Row<X> parseRow(ItemParser<X> parser, Item item) {
        ItemList list = item.getList() ;
        RowBuilder<X> builder = new RowBuilderBase<X>() ;
        
        BuilderLib.checkTag(list, "row") ;
        list = list.cdr() ;
        for (Item e : list) {
            Pair<Var, X> p = parse1(parser, e) ;
            builder.add(p.getLeft(), p.getRight()) ;
        }

        return builder.build() ;
    }

    private static <X> Pair<Var, X> parse1(ItemParser<X> parser, Item e) {
        Var var = Var.alloc(e.getList().get(0).getNode()) ;
        X x = parser.parse(e.getList().get(1)) ;
        return Pair.create(var, x) ;
    }
    
    interface ItemParser<X> { X parse(Item item)  ; }
    
    private static ItemParser<Integer> item2Int = new ItemParser<Integer>() {
        @Override
        public Integer parse(Item item) { return (int)item.asInteger() ; }
    } ;
            
    private static ItemParser<String> item2String = new ItemParser<String>() {
        @Override
        public String parse(Item item) { return item.getNode().getLiteralLexicalForm() ; }
    } ;

    
}

