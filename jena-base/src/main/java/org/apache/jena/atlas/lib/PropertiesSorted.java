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

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Comparator ;
import java.util.Enumeration ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Properties ;

/** Sorted output */
public class PropertiesSorted extends Properties
{
    private Comparator<String> comparator = null ;
    
    //public SortedProperties() { super() ; }
    
    public PropertiesSorted(Comparator<String> comparator)
    { 
        super() ;
        this.comparator = comparator ;
    }
    
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public synchronized Enumeration<Object> keys()
    {
        // Old world - enumeration, untyped. But we know they are strings (Propetries hides non-strings in get) 
        Enumeration<Object> keys = super.keys() ;
        List<String> keys2 = new ArrayList<>(super.size()) ;
        
        for( ; keys.hasMoreElements() ; )
        {
            Object obj = keys.nextElement() ;
            if ( obj instanceof String )
                keys2.add((String)obj);
        }
        // Keys are comparable because they are strings.
        if ( comparator == null )
            Collections.sort(keys2);
        else
            Collections.sort(keys2, comparator) ;
        
        return new IteratorToEnumeration(keys2.listIterator()) ;
    }
    
    static class IteratorToEnumeration<T>  implements Enumeration<T>
    {
        private Iterator<T> iterator ;

        public IteratorToEnumeration(Iterator<T> iterator)
        {
            this.iterator = iterator ;
        }
        
        @Override
        public boolean hasMoreElements()
        {
            return iterator.hasNext() ;
        }

        @Override
        public T nextElement()
        {
            return iterator.next();
        }
    }
}
