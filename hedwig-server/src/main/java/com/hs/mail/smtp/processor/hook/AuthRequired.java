/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.hs.mail.smtp.processor.hook;

import com.hs.mail.smtp.SmtpException;
import com.hs.mail.smtp.SmtpSession;
import com.hs.mail.smtp.message.MailAddress;
import com.hs.mail.smtp.message.Recipient;
import com.hs.mail.smtp.message.SmtpMessage;

public class AuthRequired implements MailHook, RcptHook {

	@Override
	public HookResult doMail(SmtpSession session, MailAddress sender) {
		return (session.getAuthID() > 0) 
				? HookResult.DUNNO 
				: HookResult.reject(SmtpException.AUTH_REQUIRED);
	}

	@Override
	public HookResult doRcpt(SmtpSession session, SmtpMessage message,
			Recipient rcpt) {
		return (session.getAuthID() > 0) ? HookResult.OK : HookResult.DUNNO;
	}

}
