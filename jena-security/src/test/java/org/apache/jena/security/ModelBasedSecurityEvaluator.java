package org.apache.jena.security;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;

public class ModelBasedSecurityEvaluator implements SecurityEvaluator {

	private Model model;
	
	public ModelBasedSecurityEvaluator( Model model) {
		this.model = model;
	}
	
	

	@Override
	public boolean evaluate(Action action, SecNode graphIRI) {
		return true;
	}

	@Override
	public boolean evaluate(Action action, SecNode graphIRI, SecTriple triple) {
		return true;
	}

	@Override
	public boolean evaluate(Set<Action> actions, SecNode graphIRI) {
		return true;
	}

	@Override
	public boolean evaluate(Set<Action> actions, SecNode graphIRI,
			SecTriple triple) {
		return true;
	}

	@Override
	public boolean evaluateAny(Set<Action> actions, SecNode graphIRI) {
		return true;
	}

	@Override
	public boolean evaluateAny(Set<Action> actions, SecNode graphIRI,
			SecTriple triple) {
		return true;
	}

	@Override
	public boolean evaluateUpdate(SecNode graphIRI, SecTriple from, SecTriple to) {
		return true;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

}
