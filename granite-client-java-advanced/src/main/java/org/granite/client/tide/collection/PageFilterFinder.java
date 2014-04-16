package org.granite.client.tide.collection;

import java.util.concurrent.Future;

import org.granite.client.tide.server.TideResponder;
import org.granite.tide.data.model.Page;
import org.granite.tide.data.model.PageInfo;

public interface PageFilterFinder<E> {

	public Future<Page<E>> find(Object filter, PageInfo page, TideResponder<Page<E>> responder);
}
