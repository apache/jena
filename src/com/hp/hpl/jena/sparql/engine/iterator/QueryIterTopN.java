package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Arrays ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;
import java.util.PriorityQueue ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.IteratorArray ;
import org.openjena.atlas.iterator.IteratorDelayedInitialization ;
import org.openjena.atlas.lib.ReverseComparator ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingComparator ;

public class QueryIterTopN extends QueryIterPlainWrapper
{
	private final QueryIterator embeddedIterator;      // Keep a record of the underlying source for .cancel.
	/* We want to keep the N least elements (overall return is an ascending sequnce so limit+ascending = least).   
	 * To do that we keep a priority heap of upto N eleemnts, ordered descending.
	 * To keep another element, it must be less than the max so far.
	 * This leaves the least N in the heap.    
	 */
    private PriorityQueue<Binding> heap ;
    long limit ;
	
    public QueryIterTopN(QueryIterator qIter, List<SortCondition> conditions, long numItems, ExecutionContext context)
    {
        this(qIter, new BindingComparator(conditions, context), numItems, context) ;
    }

    public QueryIterTopN(QueryIterator qIter, Comparator<Binding> comparator, long numItems, ExecutionContext context)
    {
        super(null, context) ;
        this.embeddedIterator = qIter;
        
        limit = numItems ;
        if ( limit == Query.NOLIMIT )
            limit = Long.MAX_VALUE ;

        if ( limit < 0 )
            throw new QueryExecException("Negative LIMIT: "+limit) ;
        
        if ( limit == 0 )
        {
            // Keep Java happy. 
            Iterator<Binding> iter0 = Iter.nullIterator() ; 
            setIterator(iter0) ;
            return ;
        }
        
        // Keep heap with maximum accessible. 
        this.heap = new PriorityQueue<Binding>((int)numItems, new ReverseComparator<Binding>(comparator)) ;
        this.setIterator(sortTopN(qIter, comparator));
    }

    @Override
    public void requestCancel()
    {
        this.embeddedIterator.cancel();
        super.requestCancel() ;
    }
    
    private Iterator<Binding> sortTopN(final QueryIterator qIter, final Comparator<Binding> comparator)
    {
        return new IteratorDelayedInitialization<Binding>() {
            @Override
            protected Iterator<Binding> initializeIterator()
            {
                for ( ; qIter.hasNext() ; )
                {
                    Binding binding = qIter.next() ;
                    if ( heap.size() < limit )
                    	heap.add(binding) ;
                    else {
                        Binding currentMaxLeastN = heap.peek() ;
                        
                    	if ( comparator.compare(binding, currentMaxLeastN) < 0 ) 
                    	{
                    	    // If binding is less than current Nth least ...
                    		heap.poll() ;     // Drop Nth least.
                        	heap.add(binding) ;
                    	}
                    }
                }
                
                Binding[] y = heap.toArray(new Binding[]{}) ;
                heap = null ;
                Arrays.sort(y, comparator) ;
                IteratorArray<Binding> iter = IteratorArray.create(y) ;
                return iter ;
        	}
		};
    }}
