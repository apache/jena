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

package org.apache.jena.atlas.lib;


/** A mutable container */
public class Ref<T>
{
    private T value ;

    public T getValue() { return value; }

    public Ref(T initial)
    { setValue(initial); }

    public void setValue(T value)
    { this.value = value; }

    
    // hashCode and equality are defined on object pointers. 
//    @Override
//    public int hashCode() { return value.hashCode()^0x1 ; }
//    
//    @Override
//    public boolean equals(Object other)
//    {
//        if ( !(  other instanceof Ref ) )
//            return false ;
//        Ref<?> r = (Ref<?>)other ;
//        return Lib.equals(value, r.value) ;
//    }
    
    @Override
    public String toString()
    {
        return "ref:"+value.toString() ; 
    }
}
