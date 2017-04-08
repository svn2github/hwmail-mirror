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
package com.hs.mail.smtp.spool;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.hs.mail.container.config.Config;
import com.hs.mail.imap.schedule.ScheduleUtils;
import com.hs.mail.mailet.Mailet;
import com.hs.mail.mailet.MailetContext;
import com.hs.mail.smtp.message.DeliveryStatusNotifier;
import com.hs.mail.smtp.message.SmtpMessage;

/**
 * 
 * @author Won Chul Doh
 * @since Jun 5, 2010
 * 
 */
public class SmtpMessageConsumer implements Consumer, InitializingBean {
	
	static Logger logger = LoggerFactory.getLogger(SmtpMessageConsumer.class);
	
    private static final long DEFAULT_DELAY_TIME = 120000; // 2*60*1000 millis (2 minutes)
	
    private MailetContext context;
	private List<Mailet> mailets;
	private long retryDelayTime = DEFAULT_DELAY_TIME;
	
	public void setRetryDelayTime(long delayTime) {
		this.retryDelayTime = delayTime;
	}
	
	public void setMailetContext(MailetContext context) {
		this.context = context;
	}

	public void setMailets(List<Mailet> mailets) {
		this.mailets = mailets;
	}

	public void afterPropertiesSet() throws Exception {
		for (Mailet aMailet : mailets) {
			aMailet.init(context);
		}
	}
	
	public int consume(Watcher watcher, Object stuffs) {
		File trigger = (File) stuffs;
		SmtpMessage message = SmtpMessage.readMessage(trigger.getName());
		
		// Check if the message is ready for processing based on the delay time.
		if (!accept(message)) {
			// We are not ready to process this.
			return Consumer.CONSUME_ERROR_KEEP;
		}

		// Save the original retry count and recipient count of this message.
		int retries = message.getRetryCount();
		int rcptcnt = message.getRecipientsSize();
		
		processMessage(message);
		
		// This means that the message was processed successfully or permanent
		// exception was caught while processing the message.
		boolean error = false;
		if (!StringUtils.isEmpty(message.getErrorMessage())) {
			// There exist errors, bounce this mail to original sender.
			dsnNotify(message);
			error = true;
		}

		// See if there are valid recipients unsent.
		if (message.getRecipientsSize() == 0) {
			// OK, we made it through... remove message from the spool.
			message.dispose();

			return (error) ? Consumer.CONSUME_ERROR_FAIL
					: Consumer.CONSUME_SUCCEEDED;
		}

		// This means temporary exception was caught while processing the
		// message. Store this message back in spool and it will get picked
		// up and processed later.
		logger.info("Storing message {} into spool after {} retries",
				message.getName(), retries);

		return retry(watcher, message, (message.getRetryCount() != retries)
				|| (message.getRecipientsSize() != rcptcnt));
	}
	
	private boolean accept(SmtpMessage message) {
		int retries = message.getRetryCount();
		if (retries > 0) {
			// Quadruples the delay with every attempt
			long timeToProcess = message.getLastUpdate().getTime()
					+ (long) Math.pow(4, retries) * retryDelayTime;
			if (System.currentTimeMillis() < timeToProcess) {
				return false;
			}
		}
		return true;
	}

	private void processMessage(SmtpMessage msg) {
		for (Mailet aMailet : mailets) {
			try {
				if (aMailet.accept(msg.getRecipients(), msg)) {
					logger.debug("Processing {} through {}",
							msg.getName(),
							aMailet.getClass().getName());
					aMailet.service(msg.getRecipients(), msg);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void dsnNotify(SmtpMessage message) {
		if (!message.isNotificationMessage()) {
			try {
				// Bounce message to the reverse-path
				DeliveryStatusNotifier.dsnNotify(null, message.getFrom(),
						message.getMimeMessage(), message.getErrorMessage());
			} catch (MessagingException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private boolean beforeLifetime(SmtpMessage message) {
		String prop = Config.getProperty("maximal_queue_lifetime", "1d");
		Date base = ScheduleUtils.getDateBefore(prop);
		return (base == null || base.before(message.getLastUpdate()));
	}
	
	private int retry(Watcher watcher, SmtpMessage message, boolean dirty) {
		try {
			if (dirty) {
				// Retry count changed or there are valid recipients unsent.
				message.setLastUpdate(new Date());
				message.store();
			}

			if (beforeLifetime(message)) {
				return Consumer.CONSUME_ERROR_KEEP;
			}

			// Consider this message as undeliverable when the time in the
			// queue has reached the maximum queue lifetime limit.
			// This message is moved to failure directory.
			message.moveTo(((FileWatcher) watcher).getFailureDir());
			return Consumer.CONSUME_ERROR_FAIL;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			message.dispose();
			return Consumer.CONSUME_ERROR_FAIL;
		}
	}

}
