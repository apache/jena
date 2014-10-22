package org.apache.jena.arq.querybuilder;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.FrontsNode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.vocabulary.RDF;

public class AbstractQueryBuilderTest {

	private AbstractQueryBuilder<?> builder;

	@Before
	public void setup() {
		builder = new TestBuilder();
	}

	private class TestBuilder extends AbstractQueryBuilder<TestBuilder> {
		@Override
		public String toString() {
			return "TestBuilder";
		}
	}

	private class NodeFront implements FrontsNode {
		Node n;

		NodeFront(Node n) {
			this.n = n;
		}

		@Override
		public Node asNode() {
			return n;
		}
	}

	@Test
	public void testMakeNode() {
		Node n = builder.makeNode(null);
		assertEquals(Node.ANY, n);

		n = builder.makeNode(RDF.type);
		assertEquals(RDF.type.asNode(), n);

		Node n2 = NodeFactory.createAnon();
		n = builder.makeNode(n2);
		assertEquals(n2, n);

		builder.addPrefix("demo", "http://example.com/");
		n = builder.makeNode("demo:type");
		assertEquals(NodeFactory.createURI("http://example.com/type"), n);

		n = builder.makeNode("<one>");
		assertEquals(NodeFactory.createURI("one"), n);

		n = builder.makeNode(builder);
		LiteralLabel ll = LiteralLabelFactory.create(builder);
		assertEquals(NodeFactory.createLiteral(ll), n);

	}

	@Test
	public void testMakeVar() {
		Var v = builder.makeVar(null);
		assertEquals(Var.ANON, v);

		v = builder.makeVar("a");
		assertEquals(Var.alloc("a"), v);

		v = builder.makeVar("?a");
		assertEquals(Var.alloc("a"), v);

		Node n = NodeFactory.createVariable("foo");
		v = builder.makeVar(n);
		assertEquals(Var.alloc("foo"), v);

		NodeFront nf = new NodeFront(n);
		v = builder.makeVar(nf);
		assertEquals(Var.alloc("foo"), v);

		v = builder.makeVar(Node_RuleVariable.WILD);
		assertNull(v);

		ExprVar ev = new ExprVar("bar");
		v = builder.makeVar(ev);
		assertEquals(Var.alloc("bar"), v);

		ev = new ExprVar(n);
		v = builder.makeVar(ev);
		assertEquals(Var.alloc("foo"), v);

		ev = new ExprVar(Var.ANON);
		v = builder.makeVar(ev);
		assertEquals(Var.ANON, v);

	}

}
