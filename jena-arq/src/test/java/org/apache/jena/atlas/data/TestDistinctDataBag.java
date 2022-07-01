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

package org.apache.jena.atlas.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File ;
import java.util.*;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.*;
import org.apache.jena.sparql.resultset.ResultSetCompare ;
import org.apache.jena.sparql.sse.Item ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.builders.BuilderBinding ;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.NodeUtils ;
import org.junit.Test ;

public class TestDistinctDataBag
{
    private static final String LETTERS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
    Random random = new Random();

    static Binding b12 = build("(?a 1) (?b 2)") ;
    static Binding b19 = build("(?a 1) (?b 9)") ;
    static Binding b02 = build("(?b 2)") ;
    static Binding b10 = build("(?a 1)") ;
    static Binding b0  = build("") ;
    static Binding bb1 = build("(?a _:XYZ) (?b 1)");
    static Binding x10 = build("(?x <http://example/abc>)") ;

    @Test public void testDistinct()
    {
        List<Binding> undistinct = new ArrayList<>();
        undistinct.add(b12);
        undistinct.add(b19);
        undistinct.add(b02);
        undistinct.add(b12);
        undistinct.add(b19);
        undistinct.add(b12);
        undistinct.add(b02);
        undistinct.add(x10);

        List<Binding> control = Iter.toList(Iter.distinct(undistinct.iterator()));
        List<Binding> distinct = new ArrayList<>();


        DistinctDataBag<Binding> db = new DistinctDataBag<>(
                new ThresholdPolicyCount<Binding>(2),
                SerializationFactoryFinder.bindingSerializationFactory(),
                new BindingComparator(new ArrayList<SortCondition>()));
        try
        {
            db.addAll(undistinct);

            Iterator<Binding> iter = db.iterator();
            while (iter.hasNext())
            {
                distinct.add(iter.next());
            }
            Iter.close(iter);
        }
        finally
        {
            db.close();
        }

        assertEquals(control.size(), distinct.size());
        assertTrue(ResultSetCompare.equalsByTest(control, distinct, NodeUtils.sameNode));
    }

    @Test public void testTemporaryFilesAreCleanedUpAfterCompletion()
    {
        List<Binding> undistinct = new ArrayList<>();
        random = new Random();
        Var[] vars = new Var[]{
            Var.alloc("1"), Var.alloc("2"), Var.alloc("3"),
            Var.alloc("4"), Var.alloc("5"), Var.alloc("6"),
            Var.alloc("7"), Var.alloc("8"), Var.alloc("9"), Var.alloc("0")
        };
        for(int i = 0; i < 500; i++){
            undistinct.add(randomBinding(vars));
        }

        DistinctDataBag<Binding> db = new DistinctDataBag<>(
                new ThresholdPolicyCount<Binding>(10),
                SerializationFactoryFinder.bindingSerializationFactory(),
                new BindingComparator(new ArrayList<SortCondition>()));

        List<File> spillFiles = new ArrayList<>();
        try
        {
            db.addAll(undistinct);
            spillFiles.addAll(db.getSpillFiles());

            int count = 0;
            for (File file : spillFiles)
            {
                if (file.exists())
                {
                    count++;
                }
            }
            // 500 bindings divided into 50 chunks (49 in files, and 1 in memory)
            assertEquals(49, count);

            Iterator<Binding> iter = db.iterator();
            while (iter.hasNext())
            {
                iter.next();
            }
            Iter.close(iter);
        }
        finally
        {
            db.close();
        }

        int count = 0;
        for (File file : spillFiles)
        {
            if (file.exists())
            {
                count++;
            }
        }
        assertEquals(0, count);
    }

    private static Binding build(String string)
    {
        Item item = SSE.parse("(binding "+string+")") ;
        return BuilderBinding.build(item) ;
    }

    private Binding randomBinding(Var[] vars)
    {
        BindingBuilder builder = Binding.builder();
        builder.add(vars[0], NodeFactory.createBlankNode());
        builder.add(vars[1], NodeFactory.createURI(randomURI()));
        builder.add(vars[2], NodeFactory.createURI(randomURI()));
        builder.add(vars[3], NodeFactory.createLiteral(randomString(20)));
        builder.add(vars[4], NodeFactory.createBlankNode());
        builder.add(vars[5], NodeFactory.createURI(randomURI()));
        builder.add(vars[6], NodeFactory.createURI(randomURI()));
        builder.add(vars[7], NodeFactory.createLiteral(randomString(5)));
        builder.add(vars[8], NodeFactory.createLiteral("" + random.nextInt(), XSDDatatype.XSDinteger));
        builder.add(vars[9], NodeFactory.createBlankNode());
        return builder.build();
    }

    public String randomURI()
    {
        return String.format("http://%s.example.com/%s", randomString(10), randomString(10));
    }

    public String randomString(int length)
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++){
            builder.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        return builder.toString();
    }

    @Test
    public void testOptionalVariables() {
        // JENA-1770
        // Setup a situation where the second binding in a spill file binds more
        // variables than the first binding.
        Binding binding1 = BindingFactory.binding(Var.alloc("1"), NodeFactory.createLiteral("A"));

        Binding binding2 = BindingFactory.binding(Var.alloc("1"), NodeFactory.createLiteral("A"),
                                                  Var.alloc("2"), NodeFactory.createLiteral("B"));

        List<Binding> undistinct = Arrays.asList(binding1, binding2, binding1);
        List<Binding> control = Iter.toList(Iter.distinct(undistinct.iterator()));
        List<Binding> distinct = new ArrayList<>();

        DistinctDataBag<Binding> db = new DistinctDataBag<>(new ThresholdPolicyCount<Binding>(2),
                                                            SerializationFactoryFinder.bindingSerializationFactory(),
                                                            new BindingComparator(new ArrayList<SortCondition>()));
        try {
            db.addAll(undistinct);
            Iterator<Binding> iter = db.iterator();
            while (iter.hasNext()) {
                distinct.add(iter.next());
            }
            Iter.close(iter);
        }
        finally {
            db.close();
        }

        assertEquals(control.size(), distinct.size());
        assertTrue(ResultSetCompare.equalsByTest(control, distinct, NodeUtils.sameNode));
    }
}
