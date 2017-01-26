/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package com.hs.mail.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Removes the dot-stuffing happening during the NNTP and SMTP message
 * transfer
 */
public class DotStuffingInputStream extends FilterInputStream {
    /**
     * An array to hold the last two bytes read off the stream.
     * This allows the stream to detect '\r\n' sequences even
     * when they occur across read boundaries.
     */
    protected int last[] = new int[2];

    public DotStuffingInputStream(InputStream in) {
        super(in);
        last[0] = -1;
        last[1] = -1;
    }

    /**
     * Read through the stream, checking for '\r\n.'
     *
     * @return the byte read from the stream
     */
    public int read() throws IOException {
        int b = in.read();
        if (b == '.' && last[0] == '\r' && last[1] == '\n') {
            //skip this '.' because it should have been stuffed
            b = in.read();
        }
        last[0] = last[1];
        last[1] = b;
        return b;
    }

    /**
     * Read through the stream, checking for '\r\n.'
     *
     * @param b the byte array into which the bytes will be read
     * @param off the offset into the byte array where the bytes will be inserted
     * @param len the maximum number of bytes to be read off the stream
     * @return the number of bytes read
     */
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
               ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;

        for (; i < len ; i++) {
            c = read();
            if (c == -1) {
                break;
            }
            if (b != null) {
                b[off + i] = (byte)c;
            }
        }

        return i;
    }
}
