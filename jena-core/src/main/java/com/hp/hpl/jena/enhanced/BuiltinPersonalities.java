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

package com.hp.hpl.jena.enhanced;
import java.io.PrintWriter ;
import java.util.Map ;

import com.hp.hpl.jena.ontology.* ;
import com.hp.hpl.jena.ontology.impl.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdf.model.impl.* ;

/**
    The personalities that are provided for the existing Jena classes. It is likely that this
    should be factored.
*/
public class BuiltinPersonalities {

    static final private Personality<RDFNode> graph = new Personality<>();

    static final public Personality<RDFNode> model = graph.copy()
        .add( Resource.class, ResourceImpl.factory )
        .add( Property.class, PropertyImpl.factory )
        .add( Literal.class,LiteralImpl.factory )
        .add( Container.class, ResourceImpl.factory )
        .add( Alt.class, AltImpl.factory )
        .add( Bag.class, BagImpl.factory )
        .add( Seq.class, SeqImpl.factory )
        .add( ReifiedStatement.class, ReifiedStatementImpl.reifiedStatementFactory )
        .add( RDFList.class, RDFListImpl.factory )

        // ontology additions
        .add( OntResource.class, OntResourceImpl.factory )
        .add( Ontology.class, OntologyImpl.factory )
        .add( OntClass.class, OntClassImpl.factory )
        .add( EnumeratedClass.class, EnumeratedClassImpl.factory )
        .add( IntersectionClass.class, IntersectionClassImpl.factory )
        .add( UnionClass.class, UnionClassImpl.factory )
        .add( ComplementClass.class, ComplementClassImpl.factory )
        .add( DataRange.class, DataRangeImpl.factory )

        .add( Restriction.class, RestrictionImpl.factory )
        .add( HasValueRestriction.class, HasValueRestrictionImpl.factory )
        .add( AllValuesFromRestriction.class, AllValuesFromRestrictionImpl.factory )
        .add( SomeValuesFromRestriction.class, SomeValuesFromRestrictionImpl.factory )
        .add( CardinalityRestriction.class, CardinalityRestrictionImpl.factory )
        .add( MinCardinalityRestriction.class, MinCardinalityRestrictionImpl.factory )
        .add( MaxCardinalityRestriction.class, MaxCardinalityRestrictionImpl.factory )
        .add( QualifiedRestriction.class, QualifiedRestrictionImpl.factory )
        .add( MinCardinalityQRestriction.class, MinCardinalityQRestrictionImpl.factory )
        .add( MaxCardinalityQRestriction.class, MaxCardinalityQRestrictionImpl.factory )
        .add( CardinalityQRestriction.class, CardinalityQRestrictionImpl.factory )

        .add( OntProperty.class, OntPropertyImpl.factory )
        .add( ObjectProperty.class, ObjectPropertyImpl.factory )
        .add( DatatypeProperty.class, DatatypePropertyImpl.factory )
        .add( TransitiveProperty.class, TransitivePropertyImpl.factory )
        .add( SymmetricProperty.class, SymmetricPropertyImpl.factory )
        .add( FunctionalProperty.class, FunctionalPropertyImpl.factory )
        .add( InverseFunctionalProperty.class, InverseFunctionalPropertyImpl.factory )
        .add( AllDifferent.class, AllDifferentImpl.factory )
        .add( Individual.class, IndividualImpl.factory )
        .add( AnnotationProperty.class, AnnotationPropertyImpl.factory )

        // Last and least ?
        .add( RDFNode.class, ResourceImpl.rdfNodeFactory )
        ;


    /**
     * For debugging purposes, list the standard personalities on the given
     * output writer.
     *
     * @param writer A printwriter to list the personalities mapping to
     */
    static public void listPersonalities( PrintWriter writer ) {
        for ( Map.Entry<Class<? extends RDFNode>, Implementation> e : model.getMap().entrySet() )
        {
            writer.println( "personality key " + e.getKey().getName() + " -> value " + e.getValue() );
        }
        writer.flush();
    }
}
