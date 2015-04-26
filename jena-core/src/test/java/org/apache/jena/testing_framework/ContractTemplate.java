package org.apache.jena.testing_framework;

import org.xenei.junit.contract.IProducer;

public class ContractTemplate<P extends IProducer<?>> {

	private P producer;

	public final void setProducer(P producer) {
		this.producer = producer;
	}

	protected final P getProducer() {
		return producer;
	}

}
