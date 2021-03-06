/*
 * Copyright 2010 the original author or authors.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.hs.mail.web.util;

/**
 * 
 * @author Won Chul Doh
 * @since Jul 5, 2007
 *
 */
public class Pager {

	// instance attributes
	private int pageNumber;	// start page is 0
	private int pageSize;
	private int itemCount;
	private boolean ascending;

	public Pager(int page, int size, int count, boolean asc) {
		this.pageSize = size;
		this.itemCount = count;
		this.ascending = asc;
		this.pageNumber = Math.min(page, Math.max(0, getPageCount() - 1));
	}
	
	public int getPageNumber() {
		return pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getItemCount() {
		return itemCount;
	}

	public int getPageCount() {
		return (int) Math.ceil((double) itemCount / (double) pageSize);
	}
	
	public boolean isAscending() {
		return ascending;
	}

	public int getBegin() {
		return (ascending) 
				? pageNumber * pageSize 
				: Math.max(0, itemCount - (pageNumber + 1) * pageSize);
	}

	public int getEnd() {
		return (ascending) 
				? Math.min(itemCount - 1, (pageNumber + 1) * pageSize - 1)
				: itemCount - pageNumber * pageSize - 1;
	}

}
