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

package org.apache.jena.assembler.assemblers;

import java.util.List;
import java.util.function.Function;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Content;
import org.apache.jena.assembler.JA;
import org.apache.jena.assembler.Mode;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF ;

public abstract class ModelAssembler extends AssemblerBase implements Assembler
    {    
    protected abstract Model openEmptyModel( Assembler a, Resource root, Mode mode );
    
    protected Model openModel( Assembler a, Resource root, Content initial, Mode mode )
        {
        Model m = openEmptyModel( a, root, mode );
        if (!initial.isEmpty()) addContent( root, m, initial );
        return m;
        }
    
    /** Execute an action in a transaction if the model supports transactions.*/
    private static void exec(Model m, Resource root, Runnable action) {
        boolean b = m.supportsTransactions();
        // No m.executeInTxn because of test MockModel. 
        if ( b ) m.begin();
        try {
            action.run();
            if ( b ) m.commit();
        }
        catch (Throwable t) {
            if ( b ) m.abort();
            throw t;
        }
    }
    
    @Override public Object open( Assembler a, Resource root, Mode mode )
        { 
        Model m = openModel( a, root, getInitialContent( a, root ), mode );
        exec(m, root, ()->{
            // JENA-1663: Do these together inside a transaction.
            addContent( root, m, getContent( a, root ) );
            addPrefixes( m, a, root );
        });
        return m; 
        }

    /** Add contents, inside a model-transaction if applicable */
    protected void addContent( Resource root, Model m, Content c ) {
        c.fill( m );
    }
    
    /** Add prefixes, inside a model-transaction if applicable */
    protected void addPrefixes(Model m, Assembler a, Resource root) {
        m.setNsPrefixes( getPrefixMapping( a, root ) );
    }
    
    private PrefixMapping getPrefixMapping( Assembler a, Resource root )
        {
        return PrefixMappingAssembler.getPrefixes
            ( a, root, PrefixMapping.Factory.create() );
        }

    @Override public Model openModel( Resource root, Mode mode )
        { return (Model) open( this, root, mode ); }

    protected Content getInitialContent( Assembler a, Resource root )
        {
        Model partial = ModelFactory.createDefaultModel();
        Resource combined = partial.createResource();
        for (StmtIterator it = root.listProperties( JA.initialContent ); it.hasNext();)
            transferContentProperties( partial, it.nextStatement().getResource(), combined );
        return contentFromModel( a, root, partial, combined );
        }

    private Content contentFromModel( Assembler a, Resource root, Model partial, Resource combined )
        {
        return partial.isEmpty()
            ? Content.empty
            : (Content) a.open( completedClone( root, combined, partial ) )
            ;
        }
    
    protected Content getContent( Assembler a, Resource root )
        {
        final Resource newRoot = oneLevelClone( root );
        final Model fragment = newRoot.getModel();
        return fragment.isEmpty() ? Content.empty : (Content) a.open( a, completedClone( root, newRoot, fragment ) );
        }

    private Resource completedClone( Resource root, Resource newRoot, Model fragment )
        {
        Model typed = fragment.add( newRoot, RDF.type, JA.Content );
        return newRoot.inModel(  ModelFactory.createUnion( root.getModel(), typed ) );
        }

    private Resource oneLevelClone( Resource root )
        {
        Model partialCopy = ModelFactory.createDefaultModel();
        Resource newRoot = partialCopy.createResource();
        transferContentProperties( partialCopy, root, newRoot );
        return newRoot;
        }

    private void transferContentProperties( Model partial, Resource someInitial, Resource combined )
        {
     	for ( Property contentProperty : ContentAssembler.contentProperties )
            {
                partial.add( copyProperties( someInitial, replaceSubjectMap( partial, combined ), contentProperty ) );
            }
        }
    
    private List<Statement> copyProperties( Resource root, Function<Statement, Statement> replace, Property property )
        { return root.listProperties( property  ).mapWith( replace ).toList(); }

	private Function<Statement, Statement> replaceSubjectMap( final Model inModel, final Resource newSubject ) {
		return s -> inModel.createStatement(newSubject, s.getPredicate(), s.getObject());
	}
	
    }
