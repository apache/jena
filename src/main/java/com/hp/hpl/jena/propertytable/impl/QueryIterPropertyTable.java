package com.hp.hpl.jena.propertytable.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter1;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.Utils;

public class QueryIterPropertyTable extends QueryIter1
	{
	    private BasicPattern pattern ;
	    private Graph graph ;
	    private QueryIterator output ;
	    
	    public static QueryIterator create(QueryIterator input,
	                                       BasicPattern pattern , 
	                                       ExecutionContext execContext)
	    {
	        return new QueryIterPropertyTable(input, pattern, execContext) ;
	    }
	    
	    private QueryIterPropertyTable(QueryIterator input,
	                                    BasicPattern pattern , 
	                                    ExecutionContext execContext)
	    {
	        super(input, execContext) ;
	        this.pattern = pattern ;
	        graph = execContext.getActiveGraph() ;
	        // Create a chain of triple iterators.
	        QueryIterator chain = getInput() ;
	        Collection<BasicPattern> patterns = sort(pattern);
	        for (BasicPattern p : patterns)
	            chain = new QueryIterPropertyTableRow(chain, p, execContext) ;
	        output = chain ;
	    }
	    
	    private Collection<BasicPattern> sort(BasicPattern pattern){
	    	HashMap<Node, BasicPattern> map= new HashMap<Node, BasicPattern>();
	    	for(Triple triple: pattern.getList()){
	    		Node subject = triple.getSubject();
	    		if(! map.containsKey(subject)){
	    			List<Triple> triples = new ArrayList<Triple>();
	    			BasicPattern p = BasicPattern.wrap(triples);
	    			map.put(subject, p);
	    			p.add(triple);
	    		}else {
	    			map.get(subject).add(triple);
	    		}
	    	}
	    	return map.values();
	    }

	    @Override
	    protected boolean hasNextBinding()
	    {
	        return output.hasNext() ;
	    }

	    @Override
	    protected Binding moveToNextBinding()
	    {
	        return output.nextBinding() ;
	    }

	    @Override
	    protected void closeSubIterator()
	    {
	        if ( output != null )
	            output.close() ;
	        output = null ;
	    }
	    
	    @Override
	    protected void requestSubCancel()
	    {
	        if ( output != null )
	            output.cancel();
	    }

	    @Override
	    protected void details(IndentedWriter out, SerializationContext sCxt)
	    {
	        out.print(Utils.className(this)) ;
	        out.println() ;
	        out.incIndent() ;
	        FmtUtils.formatPattern(out, pattern, sCxt) ;
	        out.decIndent() ;
	    }
}
