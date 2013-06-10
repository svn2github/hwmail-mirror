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
package com.hs.mail.imap.message.search;

/**
 * This abstract class implements search-criteria for Message addresses. <p>
 * 
 * @author Won Chul Doh
 * @since Jan 30, 2010
 *
 */
public abstract class AddressStringKey extends StringKey {

	protected AddressStringKey(String pattern) {
		super(pattern, true);
	}

    public boolean equals(Object obj) {
		if (!(obj instanceof AddressStringKey))
			return false;
		return super.equals(obj);
	}
	
}
