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

package org.seaborne.dboe.base;

import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.seaborne.dboe.base.block.TS_Block ;
import org.seaborne.dboe.base.file.TS_File ;
import org.seaborne.dboe.base.record.TS_Record ;
import org.seaborne.dboe.base.recordfile.TS_RecordFile ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
      TS_Block.class
    , TS_File.class
    , TS_Record.class
    , TS_RecordFile.class
})


public class TC_Base
{}
