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

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Collection ;
import java.util.List ;

import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;
import org.seaborne.dboe.engine.general.OpExecutorRowsMain ;
import org.seaborne.dboe.engine.general.OpExecutorStageMain ;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.engine.main.OpExecutorFactory ;

/** Tests of OpExecutor that work in Node space and hence on a plain in-memory dataset */
@RunWith(Parameterized.class)
public class TestOpExecutorNode extends AbstractTestOpExecutor
{
    @Parameters(name="{0}")
    public static Collection<Object[]> data()
    { 
        List<Object[]> args = new ArrayList<>() ;
        args.add(new Object[][] {{"Name", null}}) ;
        args.add(new Object[] {"Name", null}) ;
        
        return Arrays.asList(new Object[][]
            { { "OpExecutor",           OpExecutor.stdFactory }
            , { "OpExecutorStageMain",  OpExecutorStageMain.factoryMain}
            , { "OpExecutorRowsMain",   OpExecutorRowsMain.factoryRowsMain}
            }) ;                                        
    }

    public TestOpExecutorNode(String name/*ignored*/, OpExecutorFactory factory) {
        super(name, factory) ;
    }

    @Override
    protected Dataset createDataset() {
        return DatasetFactory.createMem() ;
    }
}
