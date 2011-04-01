/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 */

package dev;

public class DevSDB
{
    // Stage generator
    
    // Check named models for union graph work.
    
    // Drop support for SQL server 2000
    // - use nvarchar(max) not ntext
    // - use TOP 

    // Scope and Join bug.
    // Need to clear constant calculation across left and right in a join.
    // Temporary fix applied (TransformSDB.transform(OpJoin) does not combine SQL)
    
    // ListSubjects etc - QueryHandlerSDB
    
	// javadoc is javadoc all!

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
}
