package com.hp.hpl.jena.graph.query;

/**
	@author kers
<br>
    PredicateBase provides a base class for implementations of Predicate.
    _evaluateBool_ is left abstract - who knows what the core meaning of
    a predicate is - but a default implementation of _and_ is supplied which
    does the "obvious thing": it's _evaluateBool_ is the && of the two
    component Predicate's _evaluateBool_s.
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

