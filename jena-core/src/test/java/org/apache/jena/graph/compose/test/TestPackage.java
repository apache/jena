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

package org.apache.jena.graph.compose.test;


import junit.framework.*;
import org.apache.jena.graph.Factory ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.compose.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.test.AbstractTestPackage ;
import org.apache.jena.rdf.model.test.helpers.TestingModelFactory ;
import org.apache.jena.shared.PrefixMapping ;

public class TestPackage extends TestCase {

    public static TestSuite suite() {
    	TestSuite result = new TestSuite();

    	GraphModelFactory gmf = new GraphModelFactory(){

			@Override
			Graph getGraph()
			{
				return new Intersection(Factory.createGraphMem(), Factory.createGraphMem());
			}};

    	AbstractTestPackage atp = new AbstractTestPackage( "Intersection",  gmf ){};
    	for (int i=0;i<atp.testCount();i++)
    	{
    		result.addTest( atp.testAt(i) );
    	}

    	gmf = new GraphModelFactory(){

			@Override
			Graph getGraph()
			{
				return new Difference(Factory.createGraphMem(), Factory.createGraphMem());
			}};

    	atp = new AbstractTestPackage( "Difference",  gmf ){};
    	for (int i=0;i<atp.testCount();i++)
    	{
    		result.addTest( atp.testAt(i) );
    	}

    	gmf = new GraphModelFactory(){

			@Override
			Graph getGraph()
			{
				return new Union(Factory.createGraphMem(), Factory.createGraphMem());
			}};

    	atp = new AbstractTestPackage( "Union",  gmf ){};
    	for (int i=0;i<atp.testCount();i++)
    	{
    		result.addTest( atp.testAt(i) );
    	}
    /* */
        result.addTest( TestDelta.suite() );
        result.addTest( TestUnion.suite() );
        result.addTest( TestDisjointUnion.suite() );
        result.addTest( TestDifference.suite() );
        result.addTest( TestIntersection.suite() );
        result.addTest( TestMultiUnion.suite() );
    /* */
        result.addTest( TestPolyadicPrefixMapping.suite() );
        return  result;
    }

    private static abstract class GraphModelFactory implements TestingModelFactory
	{
    	abstract Graph getGraph();

		@Override
		public Model createModel()
		{
			return createModel( getGraph());
		}

		@Override
		public Model createModel( final Graph base )
		{
			return ModelFactory.createModelForGraph(base);
		}

		@Override
		public PrefixMapping getPrefixMapping()
		{
			return ModelFactory.createDefaultModel().getGraph()
					.getPrefixMapping();
		}
	}

}
