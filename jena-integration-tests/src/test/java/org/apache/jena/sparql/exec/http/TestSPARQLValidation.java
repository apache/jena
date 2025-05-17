package org.apache.jena.sparql.exec.http;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.walker.Walker;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.junit.Test;

public class TestSPARQLValidation {

    public static class OpValidator
        extends OpVisitorBase {

        @Override
        public void visit(OpBGP opBgp) {
            opBgp.getPattern().forEach(OpValidator::validateTriple);
        }

        // TODO Validate OpQuad, OpQuadBlock, OpPath, OpGraph etc.

        protected static void validateTriple(Triple t) {
            if (!isValidAsSPARQL(t.getSubject(), t.getPredicate(), t.getObject())) {
                throw new QueryException("Not valid");
            }
        }

        // Adapted from NodeUtils.isValidAsRDF (SPARQL variants could be added)
        public static boolean isValidAsSPARQL(Node s, Node p, Node o) {
            if ( s == null || ( ! s.isBlank() && ! s.isURI() && ! s.isVariable() ) )
                return false;
            if ( p == null || ( ! p.isURI() && ! p.isVariable() ) )
                return false;
            if ( o == null || ( ! o.isBlank() && ! o.isURI() && ! o.isLiteral() && !o.isTripleTerm() && !o.isVariable() ) )
                return false;
            return true;
        }
    }

    public static boolean isValid(Query query ) {
        try {
            Op op = Algebra.compile(query);
            OpVisitor opVisitor = new OpValidator();
            Walker.walk(op, opVisitor);
            return true;
        } catch (QueryException e) {
            return false;
        }
    }

    @Test
    public void test() {
        Query query = QueryFactory.create("SELECT * { ?s a ?t . SERVICE <urn:foobar> { ?s ?p ?o } }");
        System.out.println("is original query valid: " + isValid(query));
        Map<Var, Node> map = Map.of(Var.alloc("p"), NodeFactory.createBlankNode("bn"));
        Query substQuery = QueryTransformOps.replaceVars(query, map);
        System.out.println("is substituted query valid: " + isValid(substQuery));
    }
}
