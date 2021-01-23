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
import static org.junit.Assert.fail;

import java.io.File ;
import java.util.*;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryCancelledException ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingComparator ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.junit.Test ;

public class TestSortedDataBag
{
    private static final String LETTERS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
    private Random random;
    
    @Test public void testSorting() 
    {
        testSorting(500, 10);
    }
    
    private void testSorting(int numBindings, int threshold)
    {
        List<Binding> unsorted = randomBindings(numBindings);
        
        List<SortCondition> conditions = new ArrayList<>();
        conditions.add(new SortCondition(new ExprVar("8"), Query.ORDER_ASCENDING));
        conditions.add(new SortCondition(new ExprVar("1"), Query.ORDER_ASCENDING));
        conditions.add(new SortCondition(new ExprVar("0"), Query.ORDER_DESCENDING));
        BindingComparator comparator = new BindingComparator(conditions);

        List<Binding> sorted = new ArrayList<>();
        
        SortedDataBag<Binding> db = new SortedDataBag<>(
                new ThresholdPolicyCount<Binding>(threshold),
                SerializationFactoryFinder.bindingSerializationFactory(),
                comparator);
        try
        {
            db.addAll(unsorted);
            Iterator<Binding> iter = db.iterator(); 
            while (iter.hasNext())
            {
                sorted.add(iter.next());
            }
            Iter.close(iter);
        }
        finally
        {
            db.close();
        }
        
        Collections.sort(unsorted, comparator);
        assertEquals(unsorted, sorted);
    }
    
    @Test public void testSortingWithPreMerge() 
    {
        // Save the original value...
        int origMaxSpillFiles = SortedDataBag.MAX_SPILL_FILES;
        try
        {
            // Vary the number of spill files and bindings so we get a variable number of premerge rounds
            SortedDataBag.MAX_SPILL_FILES = 2;    testSorting(1, 1);
            SortedDataBag.MAX_SPILL_FILES = 2;    testSorting(2, 1);
            SortedDataBag.MAX_SPILL_FILES = 2;    testSorting(3, 1);
            SortedDataBag.MAX_SPILL_FILES = 2;    testSorting(4, 1);
            SortedDataBag.MAX_SPILL_FILES = 2;    testSorting(5, 1);
            SortedDataBag.MAX_SPILL_FILES = 2;    testSorting(1, 10);
            SortedDataBag.MAX_SPILL_FILES = 2;    testSorting(1000, 10);
            SortedDataBag.MAX_SPILL_FILES = 100;  testSorting(1000, 10);
            SortedDataBag.MAX_SPILL_FILES = 2;    testSorting(10, 10);
            SortedDataBag.MAX_SPILL_FILES = 5;    testSorting(10, 10);
        }
        finally
        {
            SortedDataBag.MAX_SPILL_FILES = origMaxSpillFiles;
        }
    }
    
    @Test public void testTemporaryFilesAreCleanedUpAfterCompletion()
    {
        List<Binding> unsorted = randomBindings(500);
        
        List<SortCondition> conditions = new ArrayList<>();
        conditions.add(new SortCondition(new ExprVar("8"), Query.ORDER_ASCENDING));
        BindingComparator comparator = new BindingComparator(conditions);
        
        SortedDataBag<Binding> db = new SortedDataBag<>(
                new ThresholdPolicyCount<Binding>(10),
                SerializationFactoryFinder.bindingSerializationFactory(),
                comparator);
        
        List<File> spillFiles = new ArrayList<>();
        try
        {
            db.addAll(unsorted);
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
    
    private List<Binding> randomBindings(int numBindings) 
    {
        random = new Random();
        Var[] vars = new Var[]{
            Var.alloc("1"), Var.alloc("2"), Var.alloc("3"),
            Var.alloc("4"), Var.alloc("5"), Var.alloc("6"),
            Var.alloc("7"), Var.alloc("8"), Var.alloc("9"), Var.alloc("0")
        };
        List<Binding> toReturn = new ArrayList<>();
        for(int i = 0; i < numBindings; i++){
            toReturn.add(randomBinding(vars));
        }
        
        return toReturn;
    }
    
    private Binding randomBinding(Var[] vars)
    {
        BindingMap binding = BindingFactory.create();
        binding.add(vars[0], NodeFactory.createBlankNode());
        binding.add(vars[1], NodeFactory.createURI(randomURI()));
        binding.add(vars[2], NodeFactory.createURI(randomURI()));
        binding.add(vars[3], NodeFactory.createLiteral(randomString(20)));
        binding.add(vars[4], NodeFactory.createBlankNode());
        binding.add(vars[5], NodeFactory.createURI(randomURI()));
        binding.add(vars[6], NodeFactory.createURI(randomURI()));
        binding.add(vars[7], NodeFactory.createLiteral(randomString(5)));
        binding.add(vars[8], NodeFactory.createLiteral("" + random.nextInt(), XSDDatatype.XSDinteger));
        binding.add(vars[9], NodeFactory.createBlankNode());
        return binding;
    }

    private String randomURI() 
    {
        return String.format("http://%s.example.com/%s", randomString(10), randomString(10));
    }
    
    private String randomString(int length)
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++){
            builder.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        return builder.toString();
    }
    
    private void getNextAndExpectException(QueryIterator iter) 
    {
        try{
            iter.hasNext();
            fail("Expected an exception here");
        }catch(QueryCancelledException e){
            // expected
        }catch(Exception e){
            fail("Unexpected exception");
        }
    }

}
