package org.apache.jena.testing_framework.manifest;

import org.apache.jena.n3.turtle.TurtleTestVocab;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.TestManifest;

public class ManifestItem {
	private Resource entry;

	public ManifestItem(Resource entry) {
		this.entry = entry;
	}

	public Resource getEntry() {
		return entry;
	}

	public String getTestName() {
		return Manifest.getLiteral(entry, TestManifest.name);
	}

	public Resource getAction() {
		return Manifest.getResource(entry, TestManifest.action);
	}

	public Resource getResult() {
		return Manifest.getResource(entry, TestManifest.result);
	}

	public Resource getType() {
		return Manifest.getResource(entry, RDF.type);
	}

	public Resource getOutput() {
		Resource result = getResult();
		return result == null ? null : Manifest.getResource(result,
				TurtleTestVocab.output);
	}

	public Resource getInput() {
		Resource action = getAction();
		return action == null ? null : Manifest.getResource(action,
				TurtleTestVocab.input);
	}

	public String getUriString() {
		Resource action = getAction();
		Resource inputIRIr = action == null ? null : Manifest.getResource(
				action, TurtleTestVocab.inputIRI);
		return (inputIRIr == null) ? null : inputIRIr.getURI();
	}
}
