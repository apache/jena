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

package org.seaborne.dboe.trans;

import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.seaborne.dboe.trans.bplustree.TS_TxnBPTree ;
import org.seaborne.dboe.trans.data.TS_TransData ;
import org.seaborne.dboe.trans.recovery.TestRecovery ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TS_TransData.class
    , TS_TxnBPTree.class 
    , TestRecovery.class
})
public class TC_TransData {}

