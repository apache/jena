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
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.cdt.LiteralLabelForList;
import org.apache.jena.cdt.LiteralLabelForMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.serializer.SerializationContext;

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
            if ( CompositeDatatypeList.uri.equals(dtURI) )
                return unfoldList( n.getLiteral(), inputBinding );
            if ( CompositeDatatypeMap.uri.equals(dtURI) )
                return unfoldMap( n.getLiteral(), inputBinding );
        }

        return QueryIterSingleton.create( inputBinding, getExecContext() );
    }

    protected QueryIterator unfoldList( final LiteralLabel lit, final Binding inputBinding ) {
        final Iterable<CDTValue> itListElmts = CompositeDatatypeList.getValue(lit);
        return new QueryIterUnfoldWorkerForLists(inputBinding, itListElmts);
    }

    protected QueryIterator unfoldMap( final LiteralLabel lit, final Binding inputBinding ) {
        final Iterable<Map.Entry<CDTKey,CDTValue>> itMapEntries = CompositeDatatypeMap.getValue(lit).entrySet();
        return new QueryIterUnfoldWorkerForMaps(inputBinding, itMapEntries);
    }



    protected abstract class QueryIterUnfoldWorkerBase<T> extends QueryIteratorBase {
        protected final Binding inputBinding;
        protected final Iterator<T> itElmts;

        protected QueryIterUnfoldWorkerBase(Binding inputBinding, Iterator<T> itElmts) {
            this.inputBinding = inputBinding;
            this.itElmts = itElmts;
        }

        protected QueryIterUnfoldWorkerBase(Binding inputBinding, Iterable<T> itElmts) {
            this( inputBinding, itElmts.iterator() );
        }

        @Override
        protected boolean hasNextBinding() { return itElmts.hasNext(); }

        @Override
        protected void requestCancel() { } // nothing to do really

        @Override
        protected void closeIterator() { } // nothing to do really
    }


    protected class QueryIterUnfoldWorkerForLists extends QueryIterUnfoldWorkerBase<CDTValue> {

        public QueryIterUnfoldWorkerForLists(Binding inputBinding, Iterator<CDTValue> itListElmts) {
            super(inputBinding, itListElmts);
        }

        public QueryIterUnfoldWorkerForLists(Binding inputBinding, Iterable<CDTValue> itListElmts) {
            super(inputBinding, itListElmts);
        }

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) {
            out.write("QueryIterUnfoldWorkerForLists");
        }

        @Override
        protected Binding moveToNextBinding() {
            final CDTValue nextElmt = itElmts.next();

            if ( nextElmt.isNull() ) {
                return inputBinding;
            }

            final Node value;
            if ( nextElmt.isNode() ) {
                value = nextElmt.asNode();
            }
            else if ( nextElmt.isList() ) {
                final LiteralLabel lit = new LiteralLabelForList( nextElmt.asList() );
                value = NodeFactory.createLiteral(lit);
            }
            else if ( nextElmt.isMap() ) {
                final LiteralLabel lit = new LiteralLabelForMap( nextElmt.asMap() );
                value = NodeFactory.createLiteral(lit);
            }
            else {
                throw new UnsupportedOperationException( "unexpected list element: " + nextElmt.getClass().getName() );
            }

            return BindingFactory.binding(inputBinding, var1, value);
        }
    }


    protected class QueryIterUnfoldWorkerForMaps extends QueryIterUnfoldWorkerBase<Map.Entry<CDTKey,CDTValue>> {

        public QueryIterUnfoldWorkerForMaps(Binding inputBinding, Iterator<Map.Entry<CDTKey,CDTValue>> itMapElmts) {
            super(inputBinding, itMapElmts);
        }

        public QueryIterUnfoldWorkerForMaps(Binding inputBinding, Iterable<Map.Entry<CDTKey,CDTValue>> itMapElmts) {
            super(inputBinding, itMapElmts);
        }

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) {
            out.write("QueryIterUnfoldWorkerForMaps");
        }

        @Override
        protected Binding moveToNextBinding() {
            final Map.Entry<CDTKey,CDTValue> elmt = itElmts.next();
            final CDTKey key     = elmt.getKey();
            final CDTValue value = elmt.getValue();

            final Node keyNode;
            if ( key.isNode() ) {
                keyNode = key.asNode();
            }
            else {
                throw new UnsupportedOperationException( "unexpected map key: " + key.getClass().getName() );
            }

            if ( value.isNull() ) {
                return BindingFactory.binding( inputBinding, var1, keyNode );
            }

            final Node valueNode;
            if ( value.isNode() ) {
                valueNode = value.asNode();
            }
            else if ( value.isList() ) {
                final LiteralLabel lit = new LiteralLabelForList( value.asList() );
                valueNode = NodeFactory.createLiteral(lit);
            }
            else if ( value.isMap() ) {
                final LiteralLabel lit = new LiteralLabelForMap( value.asMap() );
                valueNode = NodeFactory.createLiteral(lit);
            }
            else {
                throw new UnsupportedOperationException( "unexpected map value: " + value.getClass().getName() );
            }

            return BindingFactory.binding(inputBinding, var1, keyNode, var2, valueNode);
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
