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
package org.apache.jena.arq.querybuilder.clauses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Var;
import org.junit.After;
import org.junit.Before;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

@Contract(ValuesClause.class)
public class ValuesClauseTest<T extends ValuesClause<?>> extends AbstractClauseTest {

    // the producer we will user
    private IProducer<T> producer;

    private T valuesClause;

    @Contract.Inject
    // define the method to set producer.
    public final void setProducer(IProducer<T> producer) {
        this.producer = producer;
    }

    protected final IProducer<T> getProducer() {
        return producer;
    }

    @Before
    public final void setupValuesClauseTest() {
        valuesClause = producer.newInstance();
    }

    @After
    public final void cleanupValuesClauseTest() {
        getProducer().cleanUp(); // clean up the producer for the next run
    }

    @ContractTest
    public void testSetOneVar() {

        AbstractQueryBuilder<?> builder = valuesClause.addValueVar("?x", "foo");
        assertContainsRegex(VALUES + var("x") + OPT_SPACE + OPEN_CURLY + quote("foo") + OPT_SPACE + CLOSE_CURLY,
                builder.buildString());
    }

    @ContractTest
    public void testSetOneVarTwoValues() {

        AbstractQueryBuilder<?> builder = valuesClause.addValueVar("?x", "foo", "bar");
        assertContainsRegex(VALUES + var("x") + OPT_SPACE + OPEN_CURLY + quote("foo") + SPACE + quote("bar") + OPT_SPACE
                + CLOSE_CURLY, builder.buildString());
    }

    @ContractTest
    public void testSetTwoVarTwoValues() {

        AbstractQueryBuilder<?> builder = valuesClause.addValueVar("?x", "foo", "fu");
        builder.addValueVar("?y", "bar", "bear");

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + quote("bear") + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());
    }

    @ContractTest
    public void testSetTwoVarTwoValuesWithNull() {

        AbstractQueryBuilder<?> builder = valuesClause.addValueVar("?x", "foo", "fu");
        builder.addValueVar("?y", "bar", null);

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + "UNDEF" + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());
    }

    @ContractTest
    public void testSetTwoVarTwoValuesAddRowWithNull() {

        AbstractQueryBuilder<?> builder = valuesClause.addValueVar("?x", "foo", "fu");
        builder.addValueVar("?y", "bar", null);
        builder.addValueRow(null, "pub");

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + "UNDEF" + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + "UNDEF" + SPACE + quote("pub") + CLOSE_PAREN
                + CLOSE_CURLY, builder.buildString());
    }

    @ContractTest
    public void testSetTwoVarMismatchedValues() {

        AbstractQueryBuilder<?> builder = valuesClause.addValueVar("?x", "foo", "fu");
        builder.addValueVar("?y", "bar");

        try {
            builder.buildString();
            fail("Should have thrown QueryBuildException");
        } catch (QueryBuildException expected) {
            // do nothing
        }

        builder.addValueVar("?y", "bear");
        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + quote("bear") + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());
    }

    @ContractTest
    public void testAddingCollection() {
        Collection<?> xCol = Arrays.asList("?x", "foo", "fu");
        Collection<?> yCol = Arrays.asList("?y", "bar", null);
        AbstractQueryBuilder<?> builder = valuesClause.addValueVar(xCol).addValueVar(yCol);

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + "UNDEF" + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());

    }

    @ContractTest
    public void testAddingLists() {
        AbstractQueryBuilder<?> builder = valuesClause.addValueVar("?x", "foo", "fu").addValueVar(Var.alloc("y"), "bar",
                null);

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + "UNDEF" + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());

    }

    @ContractTest
    public void testAddingObject() {
        Object o = "?x";
        AbstractQueryBuilder<?> builder = valuesClause.addValueVar(o);
        builder.addValueVar("?y");
        builder.addValueRow("foo", "bar");
        builder.addValueRow("fu", null);

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + "UNDEF" + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());

    }

    @ContractTest
    public void testAddingMatrix() {
        Map<Object, Collection<Object>> map = new LinkedHashMap<Object, Collection<Object>>();

        map.put("?x", Arrays.asList("foo", NodeFactory.createLiteral("fu")));
        map.put(Var.alloc("y"), Arrays.asList("bar", null));
        AbstractQueryBuilder<?> builder = valuesClause.addValueVars(map);

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + "UNDEF" + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());

    }

    @ContractTest
    public void testAppendingMatrix() {
        Map<Object, Collection<Object>> map = new LinkedHashMap<Object, Collection<Object>>();

        map.put("?x", Arrays.asList("foo", NodeFactory.createLiteral("fu")));
        map.put(Var.alloc("y"), Arrays.asList("bar", null));
        AbstractQueryBuilder<?> builder = valuesClause.addValueVars(map);

        Map<Var, Collection<Node>> map2 = new LinkedHashMap<Var, Collection<Node>>();
        map2.put(Var.alloc("y"), Arrays.asList(NodeFactory.createLiteral("baz"), Var.alloc("z")));
        map2.put(Var.alloc("z"), Arrays.asList(NodeFactory.createLiteral("dog"), NodeFactory.createLiteral("cat")));

        builder.addValueVars(map2);

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + SPACE + var("z") + CLOSE_PAREN
                + OPT_SPACE + OPEN_CURLY + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + SPACE + "UNDEF"
                + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu") + SPACE + "UNDEF" + SPACE + "UNDEF" + CLOSE_PAREN
                + OPT_SPACE + OPEN_PAREN + "UNDEF" + SPACE + quote("baz") + SPACE + quote("dog") + CLOSE_PAREN
                + OPT_SPACE + OPEN_PAREN + "UNDEF" + SPACE + var("z") + SPACE + quote("cat") + CLOSE_PAREN
                + CLOSE_CURLY, builder.buildString());

    }

    @ContractTest
    public void testAddValeuRowObjects() {
        Object o = "?x";
        AbstractQueryBuilder<?> builder = valuesClause.addValueVar(o);
        builder.addValueVar("?y");
        builder.addValueRow("foo", "bar");
        builder.addValueRow("fu", null);

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + "UNDEF" + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());

    }

    @ContractTest
    public void testAddValeuRowCollections() {
        Object o = "?x";
        AbstractQueryBuilder<?> builder = valuesClause.addValueVar(o);
        builder.addValueVar("?y");
        builder.addValueRow(Arrays.asList("foo", "bar"));
        builder.addValueRow(Arrays.asList("fu", null));

        assertContainsRegex(VALUES + OPEN_PAREN + var("x") + SPACE + var("y") + CLOSE_PAREN + OPT_SPACE + OPEN_CURLY
                + OPEN_PAREN + quote("foo") + SPACE + quote("bar") + CLOSE_PAREN + OPT_SPACE + OPEN_PAREN + quote("fu")
                + SPACE + "UNDEF" + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());

    }

    @ContractTest
    public void testDataQuery() {
        // test that the getVars getMap and clear methods work.
        Object o = "?x";
        AbstractQueryBuilder<?> builder = valuesClause.addValueVar(o).addValueVar("?y").addValueRow("foo", "bar")
                .addValueRow("fu", null);

        assertFalse(builder.getValuesVars().isEmpty());
        List<Var> lst = builder.getValuesVars();
        assertEquals(2, lst.size());
        assertEquals(Var.alloc("x"), lst.get(0));
        assertEquals(Var.alloc("y"), lst.get(1));

        Map<Var, List<Node>> map = builder.getValuesMap();
        assertEquals(2, map.keySet().size());
        List<Node> nodes = map.get(Var.alloc("x"));
        assertEquals(2, nodes.size());
        assertEquals(NodeFactory.createLiteral("foo"), nodes.get(0));
        assertEquals(NodeFactory.createLiteral("fu"), nodes.get(1));

        nodes = map.get(Var.alloc("y"));
        assertEquals(2, nodes.size());
        assertEquals(NodeFactory.createLiteral("bar"), nodes.get(0));
        assertNull(nodes.get(1));

        builder.clearValues();

        assertTrue(builder.getValuesVars().isEmpty());
        assertTrue(builder.getValuesMap().isEmpty());

    }

}
