/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.rdf.model.test;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.TypeMapper ;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.AdhocDatatype ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.LiteralRequiredException ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.test.helpers.ModelHelper ;
import org.apache.jena.rdf.model.test.helpers.TestingModelFactory ;
import org.apache.jena.test.JenaTestBase ;
import org.junit.Assert;

/**
 * TestLiteralImpl - minimal, this is the first time an extra test has
 * been needed above
 * the regression testing.
 */
public class TestLiteralImpl extends AbstractModelTestBase
{
	static class UniqueValueClass1
	{
		String value;

		UniqueValueClass1( final String value )
		{
			this.value = value;
		}

		@Override
		public String toString()
		{
			return value;
		}
	}

	static class UniqueValueClass2
	{
		String value;

		UniqueValueClass2( final String value )
		{
			this.value = value;
		}

		@Override
		public String toString()
		{
			return value;
		}
	}

	public TestLiteralImpl( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	/**
	 * Test that a literal node can be as'ed into a literal.
	 */
	public void testAsLiteral()
	{
		ModelHelper.literal(model, "17").as(Literal.class);
	}

	/**
	 * Test that a non-literal node cannot be as'ed into a literal
	 */
	public void testCannotAsNonLiteral()
	{
		try
		{
			ModelHelper.resource(model, "plumPie").as(Literal.class);
			Assert.fail("non-literal cannot be converted to literal");
		}
		catch (final LiteralRequiredException l)
		{
			JenaTestBase.pass();
		}
	}

    /**
     * Test that a literal node can be as'ed into a number
     */
    public void testAsNumber()
    {
        int number = ModelHelper.literal(model, "17").getInt();
        assertEquals(17, number);
    }

    /**
     * Test that a literal that is not a number cannot be as'ed into a number
     */
    public void testCannotAsNonNumber()
    {
        try {
            Node node = NodeFactory.createLiteral("1984", XSDDatatype.XSDgYear);
            RDFNode rdfNode = model.asRDFNode(node);
            Literal literal = rdfNode.asLiteral();
            // XSDDateTime is not a Number, so getInt will fail, instead of returning 1984
            literal.getInt();
            fail("Expected DatatypeFormatException");
        } catch (DatatypeFormatException e) {
            final String message = e.getMessage();
            // displays error message
            assertTrue(message.contains("Error converting typed value to a number"));
            // displays the datatype URI
            assertTrue(message.contains("http://www.w3.org/2001/XMLSchema#gYear"));
            // displays the Java class name (ignoring the package)
            assertTrue(message.contains("XSDDateTime"));
        }
    }

	public void testInModel()
	{

		final Model m1 = createModel();
		final Model m2 = createModel();
		final Literal l1 = m1.createLiteral("17");
		final Literal l2 = l1.inModel(m2);
		Assert.assertEquals(l1, l2);
		Assert.assertSame(m2, l2.getModel());
	}

	public void testLiteralHasModel()
	{
		testLiteralHasModel(model, model.createLiteral("hello, world"));
		testLiteralHasModel(model, model.createLiteral("hello, world", "en-UK"));
		testLiteralHasModel(model, model.createLiteral("hello, world", true));
		testLiteralHasModel(model, model.createTypedLiteral("hello, world"));
		testLiteralHasModel(model, model.createTypedLiteral(false));
		testLiteralHasModel(model, model.createTypedLiteral(17));
		testLiteralHasModel(model, model.createTypedLiteral('x'));
	}

	private void testLiteralHasModel( final Model m, final Literal lit )
	{
		Assert.assertSame(m, lit.getModel());
	}

	public void testSameAdhocClassUS()
	{
		try
		{
			final UniqueValueClass1 ra = new UniqueValueClass1("rhubarb");
			final UniqueValueClass1 rb = new UniqueValueClass1("cottage");
			Assert.assertNull("not expecting registered RDF Datatype",
					TypeMapper.getInstance().getTypeByValue(ra));
			final Literal la = model.createTypedLiteral(ra); // Sets the type
																// mapper
			// - contaminates it
			// with
			// UniqueValueClass1
			final Literal lb = model.createTypedLiteral(rb);
			JenaTestBase
					.assertInstanceOf(AdhocDatatype.class, la.getDatatype());
			Assert.assertSame(la.getDatatype(), lb.getDatatype());
			Assert.assertNotNull(TypeMapper.getInstance().getTypeByValue(ra));
		}
		finally
		{
			TypeMapper.reset();
		}
	}

	// Tests are not necessarily run in order so use UniqueValueClass2
	public void testTypedLiteralTypesAndValues()
	{
		// Resource r = model.createResource( "eh:/rhubarb" );
		final UniqueValueClass2 r = new UniqueValueClass2("rhubarb");
		Assert.assertNull("not expecting registered RDF Datatype", TypeMapper
				.getInstance().getTypeByValue(r));
		final Literal typed = model.createTypedLiteral(r); // Sets the type
		// mapper -
		// contaminates it
		// with
		// UniqueValueClass2
		final Literal string = model.createLiteral(r.value);
		Assert.assertEquals(string.getLexicalForm(), typed.getLexicalForm());
		Assert.assertEquals(string.getLanguage(), typed.getLanguage());
		JenaTestBase.assertDiffer(string.getDatatypeURI(),
				typed.getDatatypeURI());
		Assert.assertNotNull(
				"a datatype should have been invented for Resource[Impl]",
				typed.getDatatype());
		JenaTestBase.assertDiffer(typed, string);
		JenaTestBase.assertDiffer(typed.getValue(), string.getValue());
		Assert.assertEquals(r, typed.getValue());
		JenaTestBase.assertDiffer(typed.hashCode(), string.hashCode());
	}
}
