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
package com.hs.mail.imap.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.Channel;

import com.hs.mail.imap.ImapConstants;
import com.hs.mail.imap.ImapSession;
import com.hs.mail.imap.mailbox.Mailbox;
import com.hs.mail.imap.mailbox.MailboxManager;
import com.hs.mail.imap.mailbox.MailboxPath;
import com.hs.mail.imap.mailbox.MailboxQuery;
import com.hs.mail.imap.message.request.AbstractListRequest;
import com.hs.mail.imap.message.request.ImapRequest;
import com.hs.mail.imap.message.responder.ListResponder;
import com.hs.mail.imap.message.responder.Responder;
import com.hs.mail.imap.message.response.ListResponse;

/**
 * 
 * @author Won Chul Doh
 * @since Apr 16, 2010
 *
 */
public abstract class AbstractListProcessor extends AbstractImapProcessor {

	@Override
	protected void doProcess(ImapSession session, ImapRequest message,
			Responder responder) throws Exception {
		doProcess(session, (AbstractListRequest) message,
				(ListResponder) responder);
	}

	private void doProcess(ImapSession session, AbstractListRequest request,
			ListResponder responder) throws Exception {
		String referenceName = request.getMailbox();
		String mailboxName = request.getPattern();
		if (StringUtils.isEmpty(mailboxName)) {
			// An empty mailbox name argument is a special request
			// to return the hierarchy delimiter and the root name of the name
			// given in the reference.
			String referenceRoot;
			if (isFullyQualifiedName(referenceName)) {
				// A qualified reference name - get the first element.
				int i = referenceName.indexOf(Mailbox.folderSeparator);
				if (i != -1) {
					referenceRoot = referenceName.substring(0, i + 1);
				} else {
					referenceRoot = referenceName;
				}
			} else {
				referenceRoot = "";
			}
			responder.untagged(request.getCommand() + " (\\Noselect) \""
					+ Mailbox.folderSeparator + "\" \"" + referenceRoot
					+ "\"\r\n");
		} else {
			if (isFullyQualifiedName(mailboxName)) {
				// If the mailboxName if fully qualified, ignore the reference
				// name.
				referenceName = "";
			} else {
				// Remove separator from the end of reference name.
				referenceName = StringUtils.removeEnd(referenceName,
						Mailbox.folderSeparator);
			}
			MailboxPath path = buildMailboxPath(session,
					MailboxPath.interpret(referenceName, mailboxName));
			doList(session, responder, path);
		}
		responder.okCompleted(request);
	}

	@Override
	protected Responder createResponder(Channel channel, ImapRequest request) {
		return new ListResponder(channel, request);
	}

	protected abstract List<Mailbox> listMailbox(long userID, MailboxPath path,
			MailboxQuery query);
	
	protected void doList(ImapSession session, ListResponder responder,
			MailboxPath path) {
		MailboxQuery query = new MailboxQuery(path.getFullName());
		if (query.containsWildcard()) {
			List<Mailbox> mailboxes = listMailbox(session.getUserID(), path,
					query);
			for (Mailbox mailbox : mailboxes) {
				responder.respond(new ListResponse(mailbox));
			}
		} else {
			// Expression is an absolute mailbox name.
			Mailbox mailbox = getMailbox(session.getUserID(), path);
			if (mailbox != null) {
				responder.respond(new ListResponse(mailbox));
			}
		}
	}
	
	protected List<Mailbox> listMailbox(long userID, MailboxPath path,
			boolean subscribed) {
		MailboxManager manager = getMailboxManager();
		List<Mailbox> children = manager.getChildren(userID, path.getUserID(),
				path.getBaseName(), subscribed);
		if (path.isPersonalNamespace()) {
			return children;
		}
		
		// LIST - "l" right is required.
		List<Long> granted = manager.getAuthorizedMailboxIDList(userID, "l"); // l_Lookup_RIGHT
		List<Mailbox> results = new ArrayList<Mailbox>();
		for (Mailbox child : children) {
			if (granted.contains(child.getMailboxID())) {
				// Unlike other commands (e.g., SELECT) the server MUST NOT
				// return a NO response if it can't list a mailbox.
				results.add(child);
			}
		}
		return results;
	}

	protected Mailbox getMailbox(long userID, MailboxPath path) {
		MailboxManager manager = getMailboxManager();
		Mailbox result = manager.getMailbox(path.getUserID(), path.getFullName());
		if (result != null) {
			if (path.isPersonalNamespace()) {
				result.setHasChildren(manager.hasChildren(result));
			} else {
				// LIST - "l" right is required.
				if (manager.hasRights(userID, result, "l")) {
					result.setHasChildren(true);
				} else {
					// Unlike other commands (e.g., SELECT) the server MUST
					// NOT return a NO response if it can’t list a mailbox.
					// So DO NOT raise MailboxNotFoundException.
					return null;
				}
			}
		}
		return result;
	}

	private static boolean isFullyQualifiedName(String name) {
		return ((name != null) 
				&& (name.startsWith(ImapConstants.SHARED_PREFIX) 
						|| name.startsWith(ImapConstants.USER_PREFIX)));
	}

}
