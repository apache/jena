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

package com.hp.hpl.jena.tdb.index.ext;

import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.recordbuffer.RecordBufferPageBase ;

public class ExtHashParams
{
    public static int calcRecordSize(RecordFactory factory, int blkSize)
    { return RecordBufferPageBase.calcRecordSize(factory, blkSize, HashBucket.FIELD_LENGTH) ; }
    
    public static int calcBlockSize(RecordFactory factory, int maxRec)
    { return RecordBufferPageBase.calcBlockSize(factory, maxRec, HashBucket.FIELD_LENGTH) ; }
}
