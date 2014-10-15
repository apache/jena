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

package com.hp.hpl.jena.tdb.store;

import org.apache.jena.atlas.lib.BitsLong ;

public class IntegerNode
{
    // 56 bits of value, including sign bit.
    public static int LEN = 56 ;
    public static int LBITS = Long.SIZE ;
    public static long MAX = (1L<< (LEN-1) )-1;
    public static long MIN = -(1L<< (LEN-1) ) ;

    public static long pack(long v) 
    {
        if ( v >= MIN && v <= MAX )
        {
            v = BitsLong.clear(v, LEN, LBITS) ;
            v = NodeId.setType(v, NodeId.INTEGER) ;
            return v ;
        }
        else
            return -1 ;
    }

    public static long unpack(long v) 
    {
        long val = BitsLong.clear(v, LEN, LBITS) ;
        // Sign extends to 64 bits.
        if ( BitsLong.isSet(val, LEN-1) )
            val = BitsLong.set(v, LEN, LBITS) ;
        return val ;
    }
}
