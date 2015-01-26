/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine;

import java.util.Arrays ;
import java.util.Collection ;

import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;
import org.seaborne.dboe.engine.tdb.OpExecutorQuackTDB ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.tdb.TDBFactory ;

@RunWith(Parameterized.class)
public class TestOpExecutorNodeId extends AbstractTestOpExecutor
{
    @Parameters(name="{0}")
    public static Collection<Object[]> data()
    { 
        return Arrays.asList(new Object[][]
            { { "OpExecutorTDB",        OpExecutorQuackTDB.factoryTDB1 }
            , { "OpExecutorQuackTDB3",  OpExecutorQuackTDB.factorySubstitute}
            , { "OpExecutorQuackTDB",   OpExecutorQuackTDB.factoryPredicateObject}
            }) ;                                        
    }

    public TestOpExecutorNodeId(String name/*ignored*/, OpExecutorFactory factory) {
        super(name, factory) ;
    }

    @Override
    protected Dataset createDataset() {
        return TDBFactory.createDataset() ;
    }
}
