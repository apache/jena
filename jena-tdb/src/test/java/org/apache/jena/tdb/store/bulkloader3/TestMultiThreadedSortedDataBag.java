/**
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

package org.apache.jena.tdb.store.bulkloader3;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.jena.tdb.store.bulkloader3.MultiThreadedSortedDataBag;
import org.junit.Before;
import org.junit.Test;
import org.openjena.atlas.data.ThresholdPolicyCount;
import org.openjena.atlas.iterator.Iter;
import org.openjena.riot.SerializationFactoryFinder;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingComparator;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.util.NodeFactory;

public class TestMultiThreadedSortedDataBag extends TestCase
{
    private List<Binding> unsorted;
    private BindingComparator comparator ;
    private MultiThreadedSortedDataBag<Binding> db ;
    
    private int N = 500 ;
    private int MAX_SPILL_FILES = 10 ;
    private int THRESHOLD = 5 ; 

    @Before @Override public void setUp() 
    {
        Random random = new Random();
        unsorted = new ArrayList<Binding>();
        Var var = Var.alloc("x");
        for(int i = 0; i < N; i++){
            unsorted.add(BindingFactory.binding(var, NodeFactory.intToNode(random.nextInt())));
        }
        
        List<SortCondition> conditions = new ArrayList<SortCondition>(); 
        conditions.add(new SortCondition(new ExprVar("x"), Query.ORDER_ASCENDING));
        comparator = new BindingComparator(conditions);
        
        db = new MultiThreadedSortedDataBag<Binding>( new ThresholdPolicyCount<Binding>(THRESHOLD), SerializationFactoryFinder.bindingSerializationFactory(), comparator);
        MultiThreadedSortedDataBag.MAX_SPILL_FILES = MAX_SPILL_FILES ;
    }

    @Test public void testSorting() 
    {
        List<Binding> sorted = new ArrayList<Binding>();
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
    
    @Test public void testTemporaryFilesAreCleanedUpAfterCompletion()
    {
        try
        {
            db.addAll(unsorted);
            
            int count = 0;
            for (File file : db.getSpillFiles())
            {
                if (file.exists())
                {
                    count++;
                }
            }
            assertEquals(N / THRESHOLD - 1, count);
            
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
        for (File file : db.getSpillFiles())
        {
            if (file.exists())
            {
                count++;
            }
        }
        assertEquals(0, count);
    }
    
}
