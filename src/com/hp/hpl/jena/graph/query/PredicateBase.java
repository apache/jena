package com.hp.hpl.jena.graph.query;

/**
    PredicateBase provides a base class for implementations of Predicate.
    <code>evaluateBool</code> is left abstract - who knows what the core meaning of
    a predicate is - but a default implementation of <code>and</code> is supplied which
    does the "obvious thing": it's <code>evaluateBool</code> is the &amp;&amp; of the two
    component Predicate's <code>evaluateBool</code>s.
    
    @author kers
*/

public abstract class PredicateBase implements Predicate
    {
    /** evaluate truth value in this domain: deferred to subclasses */
    public abstract boolean evaluateBool( Domain d );

    /** L.and(R).evaluateBool(D) = L.evaluateBool(D) && R.evaluateBool(D) */
    public Predicate and( final Predicate other )
        {
        return new PredicateBase()
            {
            public boolean evaluateBool( Domain d )
                { return PredicateBase.this.evaluateBool( d ) && other.evaluateBool( d ); }
            };
        }
    }

