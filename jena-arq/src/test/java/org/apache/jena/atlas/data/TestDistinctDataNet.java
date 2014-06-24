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

import java.io.File ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Random ;

import junit.framework.TestCase ;

import org.apache.jena.atlas.data.DistinctDataNet ;
import org.apache.jena.atlas.data.ThresholdPolicyCount ;
import org.apache.jena.atlas.iterator.Iter ;
import org.junit.Test ;
import org.apache.jena.riot.system.SerializationFactoryFinder ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingComparator ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderBinding ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;

public class TestDistinctDataNet extends TestCase
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
    
    @Test
    public void testDistinct()
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
        
        
        DistinctDataNet<Binding> db = new DistinctDataNet<>(
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
        assertTrue(ResultSetCompare.equalsByTest(control, distinct, NodeUtils.sameTerm));
    }
    
    @Test
    public void testDistinct2()
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
        
        
        DistinctDataNet<Binding> db = new DistinctDataNet<>(
                new ThresholdPolicyCount<Binding>(2),
                SerializationFactoryFinder.bindingSerializationFactory(),
                new BindingComparator(new ArrayList<SortCondition>())); 
        try
        {
            for (Binding b : undistinct)
            {
                if (db.netAdd(b))
                {
                    distinct.add(b);
                }
            }
            
            Iterator<Binding> iter = db.netIterator(); 
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
        assertTrue(ResultSetCompare.equalsByTest(control, distinct, NodeUtils.sameTerm));
    }
    
    @Test
    public void testTemporaryFilesAreCleanedUpAfterCompletion()
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
        
        DistinctDataNet<Binding> db = new DistinctDataNet<>(
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
    
    private void testDiff(String first, String second, String expected)
    {
        DistinctDataNet.SortedDiffIterator<String> sdi = DistinctDataNet.SortedDiffIterator.create(
                Arrays.asList(first.split(" ")).iterator(),
                Arrays.asList(second.split(" ")).iterator());
        
    }
    
    private void testDiff(String[] first, String[] second, String expected)
    {
        DistinctDataNet.SortedDiffIterator<String> sdi = DistinctDataNet.SortedDiffIterator.create(
                Arrays.asList(first).iterator(),
                Arrays.asList(second).iterator());
        
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        while (sdi.hasNext())
        {
            if (!firstTime)
            {
                sb.append(" ");
            }
            firstTime = false;
            
            String s = sdi.next();            
            if (null == s)
            {
                s = "null";
            }
            sb.append(s);
        }
        
        assertEquals(expected, sb.toString());
    }
    
    @Test
    public void testSortedDiffIterator()
    {
        testDiff("a b e g i j", "b g h", "a e i j");
        testDiff("a b e g i j", "", "a b e g i j");
        testDiff("", "b g h", "");
        testDiff("", "", "");
        testDiff("a", "a", "");
        testDiff("a", "b", "a");
        testDiff("b", "a", "b");
        testDiff("a b e g i j", "b g h z", "a e i j");
        testDiff("a b c", "a b c", "");
        
        testDiff(new String[] {null, "a", "b", "e", "g", "i", "j", }, new String[] { "b", "g", "h", }, "null a e i j");
        testDiff(new String[] {"a", "b", "e", "g", "i", "j", }, new String[] { null, "b", "g", "h", }, "a e i j");
        testDiff(new String[] {null, "a", "b", "e", "g", "i", "j", }, new String[] { null, "b", "g", "h", }, "a e i j");
    }
    

    private static Binding build(String string)
    {
        Item item = SSE.parse("(binding "+string+")") ;
        return BuilderBinding.build(item) ;
    }
    
    private Binding randomBinding(Var[] vars)
    {
        BindingMap binding = BindingFactory.create();
        binding.add(vars[0], NodeFactory.createAnon());
        binding.add(vars[1], NodeFactory.createURI(randomURI()));
        binding.add(vars[2], NodeFactory.createURI(randomURI()));
        binding.add(vars[3], NodeFactory.createLiteral(randomString(20)));
        binding.add(vars[4], NodeFactory.createAnon());
        binding.add(vars[5], NodeFactory.createURI(randomURI()));
        binding.add(vars[6], NodeFactory.createURI(randomURI()));
        binding.add(vars[7], NodeFactory.createLiteral(randomString(5)));
        binding.add(vars[8], NodeFactory.createLiteral("" + random.nextInt(), null, XSDDatatype.XSDinteger));
        binding.add(vars[9], NodeFactory.createAnon());
        return binding;
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
}
