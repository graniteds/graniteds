package org.granite.client.tide.collection;

import java.util.Map;
import java.util.concurrent.Future;

import org.granite.client.tide.server.TideResponder;

public interface SimpleFilterFinder<E> {

	public Future<Map<String, Object>> find(Object filter, int first, int max, String[] order, boolean[] desc, TideResponder<Map<String, Object>> responder);
}
