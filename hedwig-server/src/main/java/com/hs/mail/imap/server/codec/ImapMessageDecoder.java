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
package com.hs.mail.imap.server.codec;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

/**
 * Decodes <code>ChannelBuffer</code> into {@link ImapMessage}.
 * 
 * @author Won Chul Doh
 * @since Jan 22, 2010
 * 
 */
public abstract class ImapMessageDecoder extends
		ReplayingDecoder<ImapMessageDecoder.State> {

	// private static final String DEFAULT_PREFIX = "literal";
	
    // private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private final int maxLineLength;
	protected volatile ImapMessage message;
	private String request;
	private volatile ChannelBuffer content;
	// private volatile DeferredFileOutputStream os;

	/**
	 * The internal state of <code>ImapMessageDecoder</code>.
	 * <em>Internal use only</em>.
	 */
	protected enum State {
		READ_COMMAND, 
		READ_LITERAL, 
		READ_REMAINDER
	}

	/**
	 * Creates a new instance with the default.
	 * {@code maxLineLength (8192)}
	 */
	protected ImapMessageDecoder() {
		this(8192);
	}

	/**
	 * Creates a new instance with the specific parameter.
	 */
	protected ImapMessageDecoder(int maxLineLength) {
		super(State.READ_COMMAND, true);

		if (maxLineLength <= 0) {
			throw new IllegalArgumentException(
					"maxLineLength must be a positive integer: "
							+ maxLineLength);
		}
		this.maxLineLength = maxLineLength;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer, State state) throws Exception {
		switch (state) {
		case READ_COMMAND: {
			request = readLine(buffer, maxLineLength);
			message = createMessage(request);
			if (message.getLiteralLength() != -1) {
				checkpoint(State.READ_LITERAL);
				if (message.isNeedContinuationRequest()) {
					channel.write("+ OK\r\n");
				}
			} else {
				return message;
			}
		}
		case READ_LITERAL: {
			// we have a content-length so we just read the correct number of
			// bytes
			readFixedLengthContent(buffer);
			checkpoint(State.READ_REMAINDER);
		}
		case READ_REMAINDER: {
			// FIXME - RFC 3501 7.5
			String remainder = readLine(buffer, maxLineLength);
			return reset(remainder);
		}
		default:
			throw new Error("Shouldn't reach here.");
		}
	}

	private Object reset(String remainder) throws Exception {
		ImapMessage message = this.message;
		ChannelBuffer content = this.content;

		if (content != null) {
			if ("APPEND".equalsIgnoreCase(message.getCommand())) {
				message.setLiteral(content);
			} else {
				request = request.substring(0, request.lastIndexOf('{'))
						+ toString(content) + remainder;
				message = createMessage(request);
			}
			this.content = null;
		}
		this.message = null;

		checkpoint(State.READ_COMMAND);
		return message;
	}

	private void readFixedLengthContent(ChannelBuffer buffer) {
		long length = message.getLiteralLength();
		if (content == null) {
			content = buffer.readBytes((int) length);
		} else {
			content.writeBytes(buffer.readBytes((int) length));
		}
	}
	
	/*private void readFixedLengthChunk(ChannelBuffer buffer) throws IOException {
		if (os == null) {
			os = new DeferredFileOutputStream(DEFAULT_BUFFER_SIZE,
					File.createTempFile(DEFAULT_PREFIX, null));
		}
		IOUtils.copyLarge(
				new ChannelBufferInputStream(buffer, (int) message
						.getLiteralLength()), os);
	}*/

	protected abstract ImapMessage createMessage(String line) throws Exception;

	private String readLine(ChannelBuffer buffer, int maxLineLength)
			throws TooLongFrameException {
		StringBuilder sb = new StringBuilder(128);
		int lineLength = 0;
		while (true) {
			byte nextByte = buffer.readByte();
			if (nextByte == ImapCodecUtil.CR) {
				nextByte = buffer.readByte();
				if (nextByte == ImapCodecUtil.LF) {
					sb.append(ImapCodecUtil.CRLF);
					return sb.toString();
				}
			} else if (nextByte == ImapCodecUtil.LF) {
				sb.append((char) ImapCodecUtil.LF);
				return sb.toString();
			} else {
				if (lineLength >= maxLineLength) {
					throw new TooLongFrameException(
							"An IMAP command is larger than " + maxLineLength
									+ " bytes.");
				}
				lineLength++;
				sb.append((char) nextByte);
			}
		}
	}

	/**
	 * @see SearchRequest#decode()
	 */
	private String toString(ChannelBuffer buffer) {
		byte[] dst = new byte[buffer.readableBytes()];
		buffer.getBytes(buffer.readerIndex(), dst);
		try {
			return new String(dst, "ISO8859_1");
		} catch (UnsupportedEncodingException e) {
			return new String(dst);
		}
	}
	
}
