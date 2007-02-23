/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;

public class StageList
{
    private List stages = new ArrayList() ;

    public StageList() {}
    public StageList(StageList other) {stages.addAll(other.stages) ; }
    
    public static StageList merge(StageList sList1 , StageList sList2)
    {
        StageList x = new StageList() ;
        x.addAll(sList1) ;
        x.addAll(sList2) ;
        return x ;
    }
    
    public void add(Stage stage) { stages.add(stage) ; }
    public void addAll(StageList other) { stages.addAll(other.stages) ; }
    public void add(int i, Stage stage) { stages.add(i, stage) ; }
    
    public Stage get(int i) { return (Stage)stages.get(i) ; }
    public ListIterator iterator() { return stages.listIterator() ; } 
    public int size() { return stages.size() ; }
    
    public List getList() { return stages ; } 
    
    public QueryIterator build(QueryIterator input, ExecutionContext execCxt)
    {
        QueryIterator qIter = input ;
        for ( Iterator iter = stages.iterator() ; iter.hasNext(); )
        {
            Stage stage = (Stage)iter.next();
            qIter = stage.build(qIter, execCxt) ;
        }
        return qIter ;
    }
    
    public int hashCode() { return stages.hashCode() ; } 
    public boolean equals(Object other)
    { 
        if ( ! ( other instanceof StageList) ) 
            return false ;
        StageList sList = (StageList)other ;
        return stages.equals(sList.stages) ;
    }
    
    public String toString() { return stages.toString() ; } 
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */