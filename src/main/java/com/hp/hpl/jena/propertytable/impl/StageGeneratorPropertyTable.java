package com.hp.hpl.jena.propertytable.impl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;

public class StageGeneratorPropertyTable implements StageGenerator {

    // Using OpExecutor is preferred.
    StageGenerator above = null ;
    
    public StageGeneratorPropertyTable(StageGenerator original)
    {
        above = original ;
    }
    
    @Override
    public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        // --- In case this isn't for GraphPropertyTable
        Graph g = execCxt.getActiveGraph() ;
        
        if ( ! ( g instanceof GraphPropertyTable ) )
            // Not us - bounce up the StageGenerator chain
            return above.execute(pattern, input, execCxt) ;
        if (pattern.size() <= 1){
        	System.out.println( "<=1 "+ pattern);
        	return above.execute(pattern, input, execCxt) ;
        }
        System.out.println( ">1" + pattern);
        return QueryIterPropertyTable.create(input, pattern, execCxt);
    }
    

}
