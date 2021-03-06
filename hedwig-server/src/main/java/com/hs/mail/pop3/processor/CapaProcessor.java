/*
 * Copyright 2018 the original author or authors.
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
package com.hs.mail.pop3.processor;

import java.util.StringTokenizer;

import com.hs.mail.container.server.socket.TcpTransport;
import com.hs.mail.pop3.POP3Session;

/**
 * Handler for CAPA command.
 * 
 * @author Won Chul Doh
 * @since April 13, 2018
 * 
 */
public class CapaProcessor extends AbstractPOP3Processor {

	private final static String[] capabilities = {
		"TOP", "USER", "UIDL"
	};
	
	@Override
	void doProcess(POP3Session session, TcpTransport trans, StringTokenizer st) {
		StringBuilder responseBuffer = new StringBuilder(64)
				.append(OK_RESPONSE)
				.append(" ")
				.append("Capability list follows");
		session.writeResponse(responseBuffer.toString());
		
		for (String capability : capabilities) {
			session.writeResponse(capability);
		}

		session.writeResponse(".");
	}

}
