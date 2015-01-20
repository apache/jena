package org.apache.jena.security;

import java.util.Set;

public class StaticSecurityEvaluator implements SecurityEvaluator {

	private String user;
	
	public StaticSecurityEvaluator( String user) {
		this.user = user;
	}
	
	public void setUser( String user )
	{
		this.user = user;
	}

	@Override
	public boolean evaluate(Action action, SecNode graphIRI) {
		return true;
	}

	@Override
	public boolean evaluate(Action action, SecNode graphIRI, SecTriple triple) {
		return triple.getSubject().getValue().equals( "urn:"+getPrincipal() );
	}

	@Override
	public boolean evaluate(Set<Action> actions, SecNode graphIRI) {
		return true;
	}

	@Override
	public boolean evaluate(Set<Action> actions, SecNode graphIRI,
			SecTriple triple) {
		return triple.getSubject().getValue().equals( "urn:"+getPrincipal() );
	}

	@Override
	public boolean evaluateAny(Set<Action> actions, SecNode graphIRI) {
		return true;
	}

	@Override
	public boolean evaluateAny(Set<Action> actions, SecNode graphIRI,
			SecTriple triple) {
		return triple.getSubject().getValue().equals( "urn:"+getPrincipal() );
	}

	@Override
	public boolean evaluateUpdate(SecNode graphIRI, SecTriple from, SecTriple to) {
		return from.getSubject().getValue().equals( "urn:"+getPrincipal() ) && 
				to.getSubject().getValue().equals( "urn:"+getPrincipal() );
	}

	@Override
	public Object getPrincipal() {
		return user;
	}

}
