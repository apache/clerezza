/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.uima.utils.cl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Reads bytes from an {@link URL} to a {@link ByteBuffer}
 */
public class ResourceByteReader {

  public ByteBuffer loadResource(URL url) throws IOException {
    InputStream stream = null;
    try {
      stream = url.openStream();
      int initialCapacity = Math.min(0x40000, stream.available() + 1);
      if (initialCapacity <= 2)
        initialCapacity = 0x10000;
      else
        initialCapacity = Math.max(initialCapacity, 0x200);
      ByteBuffer buffer = ByteBuffer.allocate(initialCapacity);
      while (true) {
        if (!buffer.hasRemaining()) {
          buffer = expandBuffer(buffer);
        }
        int len = stream.read(buffer.array(), buffer.position(), buffer.remaining());
        if (len <= 0)
          break;
        buffer.position(buffer.position() + len);
      }
      buffer.flip();
      return buffer;
    } finally {
      if (stream != null) stream.close();
    }
  }

  private ByteBuffer expandBuffer(ByteBuffer buffer) {
    ByteBuffer greaterBuffer = ByteBuffer.allocate(2 * buffer.capacity());
    buffer.flip();
    greaterBuffer.put(buffer);
    buffer = greaterBuffer;
    return buffer;
  }
}
