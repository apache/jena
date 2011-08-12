package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Arrays ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;
import java.util.PriorityQueue ;

import org.openjena.atlas.iterator.IteratorDelayedInitialization ;

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
    private PriorityQueue<Binding> heap ;
    long limit ;
	
    public QueryIterTopN(QueryIterator qIter, List<SortCondition> conditions, long numItems, ExecutionContext context)
    {
        this(qIter, new BindingComparator(conditions, context), numItems, context) ;
    }

    public QueryIterTopN(QueryIterator qIter, Comparator<Binding> comparator, long numItems, ExecutionContext context)
    {
        super(null, context) ;
        
        limit = numItems ;
        if ( limit == Query.NOLIMIT )
            limit = Long.MAX_VALUE ;

        if ( limit < 0 )
            throw new QueryExecException("Negative LIMIT: "+limit) ;
        
        this.embeddedIterator = qIter;
        this.heap = new PriorityQueue<Binding>((int)numItems, comparator) ;
        
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
                    Binding b = qIter.next() ;
                    if ( heap.size() < limit )
                    	heap.add(b) ;
                    else {
                    	if ( comparator.compare(b, heap.peek()) < 0 ) {
                    		heap.poll() ;
                        	heap.add(b) ;
                    	}
                    }
                }
                
                Binding[] y = heap.toArray(new Binding[]{}) ;
                heap = null ;
                Arrays.sort(y, comparator) ;
                return Arrays.asList(y).iterator() ;
        	}
		};
    }}
