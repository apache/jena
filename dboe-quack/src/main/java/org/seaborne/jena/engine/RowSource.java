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

package org.seaborne.jena.engine;

import java.util.Iterator ;

public interface RowSource<X> extends Iterator<Row<X>> {

    /** Return true if getting the next element is likely to ready so that
     *  {@link #next} and {@link #hasNext} do not block; return
     *  false if they are likely to block.
     *  
     *  Also, return false if {@link #hasNext} is known to be false (the iterator has ended). 
     * 
     * The default implementation is to defer to {@link #hasNext}. 
     *  
     * @return boolean indicating whether there is likely to be data immediately available. 
     */
    default
    public boolean isReady() { return hasNext() ; } 
}
