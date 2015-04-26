package org.apache.jena.testing_framework.manifest;

public abstract class ManifestTest {

	protected ManifestItem manifestItem;

	public final void setManifestItem(ManifestItem manifestItem) {
		this.manifestItem = manifestItem;
	}

	abstract public void runTest();

}
