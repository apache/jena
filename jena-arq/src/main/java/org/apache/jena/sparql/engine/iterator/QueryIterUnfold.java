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

package org.apache.jena.sparql.engine.iterator;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_LiteralWithList;
import org.apache.jena.graph.Node_LiteralWithMap;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class QueryIterUnfold extends QueryIterRepeatApply
{
	protected final Expr expr ;
    protected final Var var1 ;
    protected final Var var2 ;

    public QueryIterUnfold(QueryIterator qIter, Expr expr, Var var1, Var var2, ExecutionContext execCxt) {
        super(qIter, execCxt) ;
        this.expr = expr ;
        this.var1 = var1 ;
        this.var2 = var2 ;
    }

    @Override
    protected QueryIterator nextStage(Binding inputBinding) {
        final NodeValue nv;
        try {
            nv = expr.eval( inputBinding, getExecContext() );
        }
        catch ( final ExprEvalException ex ) {
            // If the expression failed to evaluate, we create no
            // no assignment (exactly as in the case of BIND, see
            // the 'accept' method in 'QueryIterAssign')

            return QueryIterSingleton.create( inputBinding, getExecContext() );
        }

        Node n = nv.asNode();
        if ( n.isLiteral() ) {
            String dtURI = n.getLiteralDatatypeURI();
            if ( Node_LiteralWithList.datatypeUriUntypedList.equals(dtURI) )
                return unfoldUntypedList(n.getLiteralLexicalForm(), inputBinding);
            if ( Node_LiteralWithList.datatypeUriTypedList.equals(dtURI) )
                return unfoldTypedList(n.getLiteralLexicalForm(), inputBinding);
            if ( Node_LiteralWithMap.datatypeUriUntypedMap.equals(dtURI) )
                return unfoldUntypedMap(n.getLiteralLexicalForm(), inputBinding);
            if ( Node_LiteralWithMap.datatypeUriTypedMap.equals(dtURI) )
                return unfoldTypedMap(n.getLiteralLexicalForm(), inputBinding);
        }

        return QueryIterSingleton.create( inputBinding, getExecContext() );
    }

    protected QueryIterator unfoldUntypedList(String listAsValue, Binding inputBinding) {
        final PrefixMap pmap = (getExecContext().getDataset() == null) ? null : getExecContext().getDataset().prefixes();
        final Iterator<Node> itListElmts = parseUntypedList(listAsValue, pmap);
        return new QueryIterUnfoldWorkerForLists(inputBinding, itListElmts);
    }

    public static Iterator<Node> parseUntypedList( String listAsValue, final PrefixMap pmap ) {
        if ( listAsValue.startsWith("[") )
            listAsValue = listAsValue.substring( 1, listAsValue.length() - 1 );

        return parseList(listAsValue, pmap);
    }

    protected QueryIterator unfoldTypedList(String listAsValue, Binding inputBinding) {
        final PrefixMap pmap = (getExecContext().getDataset() == null) ? null : getExecContext().getDataset().prefixes();
        Iterator<Node> itListElmts = parseTypedList(listAsValue, pmap);
        return new QueryIterUnfoldWorkerForLists(inputBinding, itListElmts);
    }

    public static Iterator<Node> parseTypedList( String listAsValue, final PrefixMap pmap ) {
        listAsValue = listAsValue.substring( 1, listAsValue.lastIndexOf("]") );
        return parseList(listAsValue, pmap);
    }

    protected QueryIterator unfoldUntypedMap(String mapAsValue, Binding inputBinding) {
       final PrefixMap pmap = (getExecContext().getDataset() == null) ? null : getExecContext().getDataset().prefixes();
        Iterator<Map.Entry<Node,Node>> itMapElmts = parseUntypedMap(mapAsValue, pmap);
        return new QueryIterUnfoldWorkerForMaps(inputBinding, itMapElmts);
    }

    public static Iterator<Map.Entry<Node,Node>> parseUntypedMap( String mapAsValue, final PrefixMap pmap ) {
        if ( mapAsValue.startsWith("{") )
        	mapAsValue = mapAsValue.substring( 1, mapAsValue.length() - 1 );

        return parseMap(mapAsValue, pmap);
    }

    protected QueryIterator unfoldTypedMap(String mapAsValue, Binding inputBinding) {
        final PrefixMap pmap = (getExecContext().getDataset() == null) ? null : getExecContext().getDataset().prefixes();
        Iterator<Map.Entry<Node,Node>> itMapElmts = parseTypedMap(mapAsValue, pmap);
        return new QueryIterUnfoldWorkerForMaps(inputBinding, itMapElmts);
    }

    public static Iterator<Map.Entry<Node,Node>> parseTypedMap( String mapAsValue, final PrefixMap pmap ) {
        mapAsValue = mapAsValue.substring( 1, mapAsValue.lastIndexOf("}") );
        return parseMap(mapAsValue, pmap);
    }

    public static Iterator<Node> parseList( final String listAsValue, final PrefixMap pmap ) {
        final Iterator<String> itListElmts = extractListElements(listAsValue);
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return itListElmts.hasNext();
            }

            @Override
            public Node next() {
                final String listElmt = itListElmts.next();
                final Node n;
                if ( listElmt.startsWith("[") && listElmt.endsWith("]") )
                    n = NodeFactory.createLiteral(listElmt, Node_LiteralWithList.datatypeUntypedList);
                else if ( listElmt.startsWith("[") && ! listElmt.endsWith("]") )
                    n = NodeFactory.createLiteral(listElmt, Node_LiteralWithList.datatypeTypedList); // brittle
                else if ( listElmt.startsWith("{") && listElmt.endsWith("}") )
                    n = NodeFactory.createLiteral(listElmt, Node_LiteralWithMap.datatypeUntypedMap);
                else if ( listElmt.startsWith("{") && ! listElmt.endsWith("}") )
                    n = NodeFactory.createLiteral(listElmt, Node_LiteralWithMap.datatypeTypedMap); // brittle
                else if ( pmap != null )
                    n = NodeFactoryExtra.parseNode(listElmt, pmap);
                else
                    n = NodeFactoryExtra.parseNode(listElmt);
                return n;
            }
        };
    }

    public static Iterator<String> extractListElements(String listAsValue) {
        listAsValue = listAsValue.strip();

        if ( listAsValue.isEmpty() ) {
            return new Iterator<String>() {
                @Override public boolean hasNext() { return false; }
                @Override public String next() { throw new NoSuchElementException(); }
            };
        }

// TODO: this method needs to be improved and, in particular, made more robust in terms
//       of parsing the given lexical form of the literal; for instance, simply splitting
//       by commas is an issue if the list contains literals with commas inside -- can we
//       use existing code for parsing Turtle here?

        return new ListElementExtractor(listAsValue);
    }

    public static Iterator<Map.Entry<Node,Node>> parseMap( final String mapAsValue, final PrefixMap pmap ) {
        final Iterator<Map.Entry<String,String>> itMapElmts = extractMapElements(mapAsValue);
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return itMapElmts.hasNext();
            }

            @Override
            public Map.Entry<Node,Node> next() {
                final Map.Entry<String,String> mapElmt = itMapElmts.next();
                final String keyAsString   = mapElmt.getKey();
                final String valueAsString = mapElmt.getValue();

                final Node keyAsNode;
                if ( pmap != null )
                    keyAsNode = NodeFactoryExtra.parseNode(keyAsString, pmap );
                else
                    keyAsNode = NodeFactoryExtra.parseNode(keyAsString);

                final Node valueAsNode;
                if ( valueAsString.startsWith("[") && valueAsString.endsWith("]") )
                    valueAsNode = NodeFactory.createLiteral(valueAsString, Node_LiteralWithList.datatypeUntypedList);
                else if ( valueAsString.startsWith("[") && ! valueAsString.endsWith("]") )
                    valueAsNode = NodeFactory.createLiteral(valueAsString, Node_LiteralWithList.datatypeTypedList); // brittle
                else if ( valueAsString.startsWith("{") && valueAsString.endsWith("}") )
                    valueAsNode = NodeFactory.createLiteral(valueAsString, Node_LiteralWithMap.datatypeUntypedMap);
                else if ( valueAsString.startsWith("{") && ! valueAsString.endsWith("}") )
                    valueAsNode = NodeFactory.createLiteral(valueAsString, Node_LiteralWithMap.datatypeTypedMap); // brittle
                else if ( pmap != null )
                    valueAsNode = NodeFactoryExtra.parseNode(valueAsString, pmap);
                else
                    valueAsNode = NodeFactoryExtra.parseNode(valueAsString);

                return new Map.Entry<>() {
                    @Override public Node getKey() { return keyAsNode; }
                    @Override public Node getValue() { return valueAsNode; }
                    @Override public Node setValue(Node v) { throw new UnsupportedOperationException(); }
                };
            }
        };
    }

    public static Iterator<Map.Entry<String,String>> extractMapElements(String mapAsValue) {
    	mapAsValue = mapAsValue.strip();

        if ( mapAsValue.isEmpty() ) {
            return new Iterator<Map.Entry<String,String>>() {
                @Override public boolean hasNext() { return false; }
                @Override public Map.Entry<String,String> next() { throw new NoSuchElementException(); }
            };
        }

// TODO: this method needs to be improved and, in particular, made more robust in terms
//       of parsing the given lexical form of the literal; for instance, simply splitting
//       by commas is an issue if the list contains literals with commas inside -- can we
//       use existing code for parsing Turtle here?

        return new MapElementExtractor(mapAsValue);
    }


    protected abstract class QueryIterUnfoldWorkerBase<T> extends QueryIteratorBase {
        protected final Binding inputBinding;
        protected final Iterator<T> itElmts;

        protected QueryIterUnfoldWorkerBase(Binding inputBinding, Iterator<T> itElmts) {
            this.inputBinding = inputBinding;
            this.itElmts = itElmts;
        }

        @Override
        protected boolean hasNextBinding() { return itElmts.hasNext(); }

        @Override
        protected void requestCancel() { } // nothing to do really

        @Override
        protected void closeIterator() { } // nothing to do really
    }


    protected class QueryIterUnfoldWorkerForLists extends QueryIterUnfoldWorkerBase<Node> {

        public QueryIterUnfoldWorkerForLists(Binding inputBinding, Iterator<Node> itListElmts) {
            super(inputBinding, itListElmts);
        }

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) {
            out.write("QueryIterUnfoldWorkerForLists");
        }

        @Override
        protected Binding moveToNextBinding() {
            return BindingFactory.binding( inputBinding, var1, itElmts.next() );
        }
    }


    protected class QueryIterUnfoldWorkerForMaps extends QueryIterUnfoldWorkerBase<Map.Entry<Node,Node>> {

        public QueryIterUnfoldWorkerForMaps(Binding inputBinding, Iterator<Map.Entry<Node,Node>> itMapElmts) {
            super(inputBinding, itMapElmts);
        }

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) {
            out.write("QueryIterUnfoldWorkerForMaps");
        }

        @Override
        protected Binding moveToNextBinding() {
            final Map.Entry<Node,Node> elmt = itElmts.next();
            if ( var2 == null )
                return BindingFactory.binding( inputBinding, var1, elmt.getKey() );
            else
                return BindingFactory.binding( inputBinding, var1, elmt.getKey(), var2, elmt.getValue() );
        }
    }


    protected static abstract class ElementExtractorBase<T> implements Iterator<T> {
        protected final String str;
        protected int cursor = 0;
        private T nextElmt = null;

        protected ElementExtractorBase( final String str ) {
            this.str = str.strip();
        }

        @Override
        public boolean hasNext() {
            if ( nextElmt != null )
                return true;

            nextElmt = produceNext();
            return ( nextElmt != null );
        }

        @Override
        public T next() {
            if ( ! hasNext() )
                throw new NoSuchElementException();

            final T r = nextElmt;
            nextElmt = null;
            return r;
        }

        protected abstract T produceNext();

        protected void consumeWhiteSpace() {
            while ( cursor < str.length() && str.charAt(cursor) == ' ' )
                cursor++;
        }

        protected void advanceToEndOfSubList() {
            while ( cursor < str.length() && str.charAt(cursor) != ']' ) {
                cursor++;
                if ( str.charAt(cursor) == '[' ) {
                    advanceToEndOfSubList();
                    cursor++;
                }
                else if ( str.charAt(cursor) == '{' ) {
                    advanceToEndOfSubMap();
                    cursor++;
                }
            }
        }

        protected void advanceToEndOfSubMap() {
            while ( cursor < str.length() && str.charAt(cursor) != '}' ) {
                cursor++;
                if ( str.charAt(cursor) == '[' ) {
                    advanceToEndOfSubList();
                    cursor++;
                }
                else if ( str.charAt(cursor) == '{' ) {
                    advanceToEndOfSubMap();
                    cursor++;
                }
            }
        }

        protected void advanceToNextCommaOrEnd() {
            while ( cursor < str.length() && str.charAt(cursor) != ',' )
                cursor++;
        }
    }


    protected static class ListElementExtractor extends ElementExtractorBase<String> {
        public ListElementExtractor( final String listAsString ) {
            super(listAsString);
        }

        @Override
        public String produceNext() {
            consumeWhiteSpace();
            if ( cursor >= str.length() ) {
                return null;
            }

            final int nextElmtBegin = cursor;
            if ( str.charAt(cursor) == '[' ) {
                advanceToEndOfSubList();
            }
            else if ( str.charAt(cursor) == '{' ) {
                advanceToEndOfSubMap();
            }
            advanceToNextCommaOrEnd();
            final String nextElmt = str.substring(nextElmtBegin, cursor).strip();
            cursor++; // move cursor to after comma
            return nextElmt;
        }
    }


    protected static class MapElementExtractor extends ElementExtractorBase<Map.Entry<String,String>> {
        public MapElementExtractor( final String mapAsString ) {
            super(mapAsString);
        }

        @Override
        public Map.Entry<String,String> produceNext() {
            consumeWhiteSpace();
            if ( cursor >= str.length() ) {
                return null;
            }

            final int nextKeyBegin = cursor;
            advanceToNextColon();
            final String nextKey = str.substring(nextKeyBegin, cursor).strip();

            cursor++; // move cursor to after colon
            consumeWhiteSpace();
            final int nextValueBegin = cursor;
            if ( str.charAt(cursor) == '[' ) {
                advanceToEndOfSubList();
            }
            else if ( str.charAt(cursor) == '{' ) {
                advanceToEndOfSubMap();
            }
            advanceToNextCommaOrEnd();
            final String nextValue = str.substring(nextValueBegin, cursor).strip();
            cursor++; // move cursor to after comma

            return new Map.Entry<>() {
                @Override public String getKey() { return nextKey; }
                @Override public String getValue() { return nextValue; }
                @Override public String setValue(String v) { throw new UnsupportedOperationException(); }
            };
        }

        protected void advanceToNextColon() {
            while ( cursor < str.length() && str.charAt(cursor) != ':' )
                cursor++;
        }
    }

}
