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

import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.seaborne.dboe.engine.join.TS_Join ;
import org.seaborne.dboe.engine.tdb.TS_Engine2 ;
import org.seaborne.tdb2.TDB2 ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    
    TS_Join.class
    //, TS_Join2.class // Second phase code - to be intergated.
    , TS_Access.class
    // Node space
    
    // TDB
    , TS_Engine2.class
    // From TDB directly.
    , TS_AsTDB.class 
    , TS_QuackEngines.class
    
} )

public class TC_Quack {
    
    @BeforeClass static public void beforeClass() {
        TDB2.init();
        Quack.init() ;
        // Assumes POS, PSO
        Quack.hardRewire() ;
        Quack2.init() ;
    }
}
