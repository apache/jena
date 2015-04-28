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

import static java.lang.String.format ;

import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.AtlasException ;



/** General descriptor of a reordering (mapping) of columns in tuples to columns in indexes, 
 * for example, from triples to triple index order. 
 *
 * Naming: map is convert to the reordered form, fetch is get back.
 */
public class ColumnMap
{
    // Map from tuple order to index order
    // So SPO->POS is (0->2, 1->0, 2->1)
    // i.e. the location of the element after mapping.  
    private int[] insertOrder ;            
    
    // The mapping from index to tuple order
    // For POS->SPO, is (0->1, 1->2, 2->0)
    // i.e. the location to fetch the mapped element from. 
    private int[] fetchOrder ;

    private String label ;

    /** Construct a column mapping that maps the input (one col, one char) to the output */  
    public ColumnMap(String input, String output)
    {
        this(input+"->"+output, compileMapping(input, output)) ;
    }
    
    public <T> ColumnMap(String label, List<T> input, List<T> output)
    {
        this(label, compileMapping(input, output)) ;
    }
    
    public <T> ColumnMap(String label, T[] input, T[] output)
    {
        this(label, compileMapping(input, output)) ;
    }
    
    /** Construct a column map - the elements are the 
     * mappings of a tuple originally in the order 0,1,2,...
     * so SPO->POS is 2,0,1 (SPO->POS so S->2, P->0, O->1)   
     * and not 1,2,0 (which is the extraction mapping).
     * The label is just a lable and is not interpretted.
     */
    public ColumnMap(String label, int...elements)
    {
        this.label = label ;

        this.insertOrder = new int[elements.length] ;
        System.arraycopy(elements, 0, elements, 0, elements.length) ;
        Arrays.fill(insertOrder, -1) ;
        
        this.fetchOrder = new int[elements.length] ;
        Arrays.fill(fetchOrder, -1) ;
    
        for ( int i = 0 ; i < elements.length ; i++ )
        {
            int x = elements[i] ;
            if ( x < 0 || x >= elements.length)
                throw new IllegalArgumentException("Out of range: "+x) ;
            // Checking
            if ( insertOrder[i] != -1 || fetchOrder[x] != -1 )
                throw new IllegalArgumentException("Inconsistent: "+ListUtils.str(elements)) ;
            
            insertOrder[i] = x ;
            fetchOrder[x] = i ;
        }
    }
    
    /** Length of mapping */
    
    public int length() { return fetchOrder.length ; }
    
    /** Apply to an <em>unmapped</em> tuple to get the i'th slot after mapping : SPO->POS : 0'th slot is P from SPO */
    public <T> T fetchSlot(int idx, Tuple<T> tuple)
    { 
        idx = fetchOrder[idx] ;     // Apply the reverse mapping as we are doing zero is P, so it's an unmap.
        return tuple.get(idx) ;
    }

    /** Apply to an <em>unmapped</em> tuple to get the i'th slot after mapping : SPO->POS : 0'th slot is P from SPO */
    public <T> T fetchSlot(int idx, T[] tuple)
    { 
        idx = fetchOrder[idx] ;     // Apply the reverse mapping as we are doing zero is P, so it's an unmap.
        return tuple[idx] ;
    }
    
    /** Apply to a <em>mapped</em> tuple to get the i'th slot as it appears after mapping : SPO->POS : 0'th slot is S from POS */
    public <T> T mapSlot(int idx, Tuple<T> tuple)
    { 
        idx = insertOrder[idx] ;
        return tuple.get(idx) ;
    }
    
    /** Apply to a <em>mapped</em> tuple to get the i'th slot as it appears after mapping : SPO->POS : 0'th slot is S from POS */
    public <T> T mapSlot(int idx, T[] tuple)
    { 
        idx = insertOrder[idx] ;        // Yes - it's the insert location we want to access 
        return tuple[idx] ;
    }
    
    /** Get the index of the i'th slot as it appears after mapping : SPO->POS : 0'th slot is S from POS so 2->0 */
    public int mapSlotIdx(int idx)
    { 
        return insertOrder[idx] ;        // Yes - it's the insert location we want to access 
    }

    /** Get the index of the i'th slot as it appears from a mapping : for SPO->POS : 0'th slot is P so 1->0 */
    public int fetchSlotIdx(int idx)
    { 
        return fetchOrder[idx] ;        // Yes - it's the insert location we want to access 
    }

    /** Apply to an <em>unmapped</em> tuple to get a tuple with the column mapping applied */
    public <T> Tuple<T> map(Tuple<T> src)
    {
        return map(src, insertOrder) ;
    }

    /** Apply to a <em>mapped</em> tuple to get a tuple with the column mapping reverse-applied */
    public <T> Tuple<T> unmap(Tuple<T> src)
    {
        return map(src, fetchOrder) ;
    }

    private <T> Tuple<T> map(Tuple<T> src, int[] map)
    {
        @SuppressWarnings("unchecked")
        T[] elts = (T[])new Object[src.size()] ;
        
        for ( int i = 0 ; i < src.size() ; i++ )
        {
            int j = map[i] ;
            elts[j] = src.get(i) ;
        }
        return Tuple.create(elts) ;
    }
    
    /** Compile a mapping encoded as single charcaters e.g. "SPO", "POS" */
    static int[] compileMapping(String domain, String range)
    {
        List<Character> input = StrUtils.toCharList(domain) ;
        List<Character> output = StrUtils.toCharList(range) ;
        return compileMapping(input, output) ;
    }

    /** Compile a mapping, encoded two list, the domain and range of the mapping function  */
    static <T> int[] compileMapping(T[] domain, T[] range)
    {
        return compileMapping(Arrays.asList(domain), Arrays.asList(range)) ;
    }
    
    /** Compile a mapping */
    static <T> int[] compileMapping(List<T> domain, List<T>range)
    {
        if ( domain.size() != range.size() )
            throw new AtlasException("Bad mapping: lengths not the same: "+domain+" -> "+range) ; 
        
        int[] cols = new int[domain.size()] ;
        boolean[] mapped = new boolean[domain.size()] ;
        //Arrays.fill(mapped, false) ;
        
        for ( int i = 0 ; i < domain.size() ; i++ )
        {
            T input = domain.get(i) ;
            int j = range.indexOf(input) ;
            if ( j < 0 )
                throw new AtlasException("Bad mapping: missing mapping: "+domain+" -> "+range) ;
            if ( mapped[j] )
                throw new AtlasException("Bad mapping: duplicate: "+domain+" -> "+range) ;
            cols[i] = j ;
            mapped[j] = true ;
        }
        return cols ;
    }
    
    @Override
    public String toString()
    {
        //return label ; 
        return format("%s:%s%s", label, mapStr(insertOrder), mapStr(fetchOrder)) ;
    }

    private Object mapStr(int[] map)
    {
        StringBuilder buff = new StringBuilder() ;
        String sep = "{" ;
        
        for ( int i = 0 ; i < map.length ; i++ )
        {
            buff.append(sep) ;
            sep = ", " ; 
            buff.append(format("%d->%d", i, map[i])) ;
        }
        buff.append("}") ;
        
        return buff.toString() ;
    }

    public String getLabel()
    {
        return label ;
    }
    
    /** Reorder the letters of a string by the same rules as this column map (forward, map direction)*/ 
    public String mapName(String word)
    {
        return mapString(word, insertOrder) ;
    }
    
    /** Reorder the letters of a string by the same rules as this column map (backward, fetch direction) */ 
    public String unmapName(String word)
    {
        return mapString(word, fetchOrder) ;
    }
    
    // Map is get from i and put to j
    private String mapString(String src, int[] map)
    {
        char[] chars = new char[src.length()] ;
        for ( int i = 0 ; i < src.length() ; i++ )
        {
            int j = map[i] ;
            chars[j] = src.charAt(i) ;
        }
        return new String(chars) ;
    }
}
