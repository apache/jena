/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class DevSDB
{
    // Document assembler for models including named graphs.

    // maven: 
    //   Assembly: Store/
    //   Update in line with ARQ
    // Jars?
    
    
	// MySQL cursors: jdbc:mysql://127.0.0.1/rdswip?useCursorFetch=true&defaultFetchSize=8192&profileSQL=false&enableQueryTimeouts=false&netTimeoutForStreamingResults=0 

    // Slot compilation / index form. 
    // Slightly better would be keep constant lookups separate from the SqlNode expression until the
    // unit is compiled.  Currently, can end up with multiple lookups of the same thing (but they will be
    // cached in the DB but if not, the query is very expensive anyway and an extra lookup will not
    // add obseravble cost).
    
    // QueryEngineSDB.init ; enable some static optimizations.
    
    // Testing: SDBTestAll does not include the model tests yet because they are not linked to the store description files
    // ParamAllStores for JUnit paramterized tests approach.
    
    // For DISTINCT do in a subquery on the node ids.
    // Similarly, push in the PROJECT to the node ids phase. 
}
