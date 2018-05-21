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

package org.apache.jena.dboe.base.record;

import java.nio.ByteBuffer ;

/** Allow bytes to pull directly out of storage with no copy to record;
 *  also extract the key bytes into an array.
 *  This operation MUST not retain any references to the ByteBuffer storage space.
 *  All in order to avoid remapping Records to higher level objects,
 *  which churns objects (GC issue) and avoids a copy.
 */
public interface RecordMapper<X> {
    X map(ByteBuffer bb, int entryIdx, byte key[], RecordFactory recFactory) ;
}
