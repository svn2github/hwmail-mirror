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

import java.util.Date;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author Won Chul Doh
 * @since Feb 17, 2007
 *
 */
public class MailUtils {
	
	public static void validateAddress(String address) throws ParseException {
		// When the strict flag is set true, InternetAddress checks that the
		// address is a valid "mailbox" per RFC822.
		new InternetAddress(address, true);
	}

	public static Date getReceivedDate(Message msg) {
		try {
			Date date = msg.getReceivedDate();
			if (null == date) {
				String[] s = msg.getHeader("Received");
				if (null == s) {
					if ((s = msg.getHeader("Date")) == null) {
						return new Date();
					}
				} else {
					// RFC 2822 - Internet Message Format
					// received = "Received:" name-val-list ";" date-time CRLF
					StringTokenizer st = new StringTokenizer(s[0], "\n");
					while (st.hasMoreTokens()) {
						s[0] = st.nextToken();
					}
					s[0] = s[0].substring(s[0].indexOf(";")+1).trim();
				}
				MailDateFormat mdf = new MailDateFormat();
				try {
					date = mdf.parse(s[0]);
				} catch (java.text.ParseException pex) {
					return new Date();
				}
			}
			return date;
		} catch (MessagingException mes) {
			return new Date();
		}
	}

	public static String getString(Message msg, String header) {
		try {
			String[] s = msg.getHeader(header);
			if (s != null) {
				return MimeUtility.decodeText(s[0]);
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	public static String[] remove(String[] array, String start, String end) {
		if (ArrayUtils.isEmpty(array)) {
			return array;
		}
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = StringUtils.removeStart(array[i], start);
			result[i] = StringUtils.removeEnd(result[i], end);
		}
		return result;
	}

}
