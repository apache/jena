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

package org.apache.jena.sparql.pfunction.library ;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.util.IterLib;

/**
 * Property function that requires the object to
 * contain a list of two items, the first of which is a string to be split, and
 * the second is a regular expression denoting the split point. If the subject
 * is an unbound variable, it is bound for each result of the split, and each result has the
 * whitespace trimmed from it. If the subject is not an unbound variable, then
 * the property function will match if and only if the subject is one of the
 * split results.
 */
public class strSplit extends PFuncSimpleAndList
{
    
    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        super.build(argSubject, predicate, argObject, execCxt);

        if (argObject.getArgListSize() != 2)
            throw new QueryBuildException("Object list must contain exactly two arguments, the string to split and a regular expression") ;
    }

    @Override
    public QueryIterator execEvaluated(final Binding binding, final Node subject, final Node predicate, final PropFuncArg object, final ExecutionContext execCxt)
    {

        if (!object.getArg(0).isLiteral() || !object.getArg(1).isLiteral()) {
            return IterLib.noResults(execCxt);
        }
        
        String s = object.getArg(0).getLiteralLexicalForm() ;
        String regex = object.getArg(1).getLiteralLexicalForm() ;
        
        // StrUtils will also trim whitespace
        List<String> tokens = Arrays.asList(StrUtils.split(s, regex));
        
        if (Var.isVar(subject)) {
            
            // Case: Subject is variable. Return all tokens as results.
            
            final Var subjectVar = Var.alloc(subject);

            Iterator<Binding> it = Iter.map(
                    tokens.iterator(),
                    item -> BindingFactory.binding(binding, subjectVar,
                            NodeFactory.createLiteral(item)));
            return new QueryIterPlainWrapper(it, execCxt);
            
        } else if ( Util.isSimpleString(subject) ) {
            // Case: Subject is a plain literal.
            // Return input unchanged if it is one of the tokens, or nothing otherwise
            if (tokens.contains(subject.getLiteralLexicalForm())) {
                return IterLib.result(binding, execCxt);
            } else {
                return IterLib.noResults(execCxt);
            }
            
        }
        
        // Any other case: Return nothing
        return IterLib.noResults(execCxt);
    }

}
