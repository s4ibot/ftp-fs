/*
 * FTPFileSystemInputStreamTest.java
 * Copyright 2016 Rob Spoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robtimus.filesystems.ftp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Test;
import org.mockftpserver.fake.filesystem.FileEntry;

@SuppressWarnings({ "nls", "javadoc" })
public class FTPFileSystemInputStreamTest extends AbstractFTPFileSystemTest {

    @Test
    public void testReadSingle() throws IOException {
        final String content = "Hello World";

        FileEntry file = addFile("/foo");
        file.setContents(content);

        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals('H', input.read());
            assertEquals('e', input.read());
            assertEquals('l', input.read());
            assertEquals('l', input.read());
            assertEquals('o', input.read());
            assertEquals(' ', input.read());
            assertEquals('W', input.read());
            assertEquals('o', input.read());
            assertEquals('r', input.read());
            assertEquals('l', input.read());
            assertEquals('d', input.read());
            assertEquals(-1, input.read());
        }
    }

    @Test
    public void testReadBulk() throws IOException {
        final String content = "Hello World";

        FileEntry file = addFile("/foo");
        file.setContents(content);

        byte[] b = new byte[20];
        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(0, input.read(b, 0, 0));
            assertEquals(5, input.read(b, 1, 5));
            assertArrayEquals(content.substring(0, 5).getBytes(), Arrays.copyOfRange(b, 1, 6));
            assertEquals(content.length() - 5, input.read(b));
            assertArrayEquals(content.substring(5).getBytes(), Arrays.copyOfRange(b, 0, content.length() - 5));
            assertEquals(-1, input.read(b));
        }
    }

    @Test
    public void testSkip() throws IOException {
        final String content = "Hello World";

        FileEntry file = addFile("/foo");
        file.setContents(content);

        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(0, input.skip(0));
            assertArrayEquals(content.getBytes(), readRemaining(input));
        }
        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(5, input.skip(5));
            assertArrayEquals(content.substring(5).getBytes(), readRemaining(input));
        }
        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(content.length(), input.skip(content.length()));
            assertEquals(-1, input.read());
            assertEquals(0, input.skip(1));
        }
        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(content.length(), input.skip(content.length() + 1));
            assertEquals(-1, input.read());
            assertEquals(0, input.skip(1));
        }
    }

    @Test
    public void testAvailable() throws IOException {
        final String content = "Hello World";

        FileEntry file = addFile("/foo");
        file.setContents(content);

        try (InputStream input = getFileSystem().newInputStream(createPath("/foo"))) {
            assertEquals(content.length(), input.available());

            for (int i = 0; i < 5; i++) {
                input.read();
            }
            assertEquals(content.length() - 5, input.available());
            while (input.read() != -1) {
                // do nothing
            }
            assertEquals(0, input.available());

            input.read();
            assertEquals(0, input.available());
        }
    }

    private byte[] readRemaining(InputStream input) throws IOException {
        // available is safe to use here
        byte[] b = new byte[input.available()];
        assertEquals(b.length, input.read(b));
        return b;
    }
}