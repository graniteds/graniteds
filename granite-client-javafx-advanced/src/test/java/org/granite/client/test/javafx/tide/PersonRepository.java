package org.granite.client.test.javafx.tide;

import java.util.concurrent.Future;

import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.server.TideResponder;
import org.granite.tide.data.model.Page;
import org.granite.tide.data.model.PageInfo;

public class PersonRepository extends ComponentImpl {

	public Future<Page<Person>> findByFilter(Object filter, PageInfo pageInfo, TideResponder<Page<Person>> responder) {
		return null;
	}
}
