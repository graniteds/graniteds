/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.tide.data.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

/**
 * @author William DRAI
 */
public class Page<E> implements Externalizable {
	
	private static final long serialVersionUID = 1L;
	
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
