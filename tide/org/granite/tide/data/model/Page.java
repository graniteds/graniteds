package org.granite.tide.data.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;


public class Page<E> implements Externalizable {
	
	private int firstResult;
	private int maxResults;
	private int resultCount;
	private List<E> resultList;
	
	
	public Page() {		
	}
	
	public Page(List<E> list) {
		this.resultList = list;
	}
	
	public Page(int firstResult, int maxResults, int resultCount, List<E> list) {
		this.firstResult = firstResult;
		this.maxResults = maxResults;
		this.resultCount = resultCount;
		this.resultList = list;
	}
	
	public int getFirstResult() {
		return firstResult;
	}
	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}
	
	public int getMaxResults() {
		return maxResults;
	}
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	
	public int getResultCount() {
		return resultCount;
	}
	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}
	
	public List<E> getResultList() {
		return resultList;
	}
	public void setResultList(List<E> resultList) {
		this.resultList = resultList;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(firstResult);
		out.writeObject(maxResults);
		out.writeObject(resultCount);
		out.writeObject(resultList);
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		firstResult = (Integer)in.readObject();
		maxResults = (Integer)in.readObject();
		resultCount = (Integer)in.readObject();
		resultList = (List<E>)in.readObject();
	}

}
