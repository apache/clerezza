/*
 * Copyright  2002-2004 WYMIWYG (www.wymiwyg.org)
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
 *
 */
package org.apache.clerezza.jaxrs.utils.form;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author reto
 * 
 */
public class MultiPartBodyImpl extends AbstractParameterCollection implements
        MultiPartBody {

    /**
     * @author reto
     * 
     */
    public static class FormFileImpl implements FormFile {

        private byte[] content;
        private String fileName;
        private MediaType type;

        /**
         * @param fileName
         * @param type
         * @param content
         */
        public FormFileImpl(String fileName, MediaType type, byte[] content) {
            this.type = type;
            this.fileName = fileName;
            this.content = content;
        }

        public byte[] getContent() {
            return content;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.wymiwyg.wrhapi.util.bodyparser.FormFile#getFileName()
         */
        public String getFileName() {
            return fileName;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.wymiwyg.wrhapi.util.bodyparser.FormFile#getMediaType()
         */
        public MediaType getMediaType() {
            return type;
        }

    }

    /**
     * @author reto
     * 
     */
    static class Disposition {

        private String fileName;

        private String name;

        /**
         * @param dispositionString
         * @throws IOException
         */
        public Disposition(String dispositionString) throws IOException {
            if (dispositionString == null) {
                throw new RuntimeException(
                        "No content-disposition string - contact your browser vendor");
            }
            StringTokenizer tokens = new StringTokenizer(dispositionString, ";");
            while (tokens.hasMoreTokens()) {
                String current = tokens.nextToken();
                current = current.trim();
                if (current.startsWith("name")) {
                    name = readTokenValue(current);
                } else {
                    if (current.startsWith("filename")) {
                        fileName = readTokenValue(current);
                    }
                }
            }
        }

        /**
         * @return
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * returns teh value between "
         * 
         * @param current
         * @return
         */
        private String readTokenValue(String token) throws IOException {
            StringWriter out = new StringWriter();
            StringReader in = new StringReader(token);
            boolean inValueSection = false;
            ;
            for (int ch = in.read(); ch != -1; ch = in.read()) {
                // assume " cannot be escaped in names
                if (ch == '\"') {
                    if (inValueSection) {
                        return out.toString();
                    } else {
                        inValueSection = true;
                    }
                } else {
                    if (inValueSection) {
                        out.write(ch);
                    }
                }
            }
            throw new IOException("token-value not terminated with \"");
        }

    }

    /**
     * the maximum size of the parsed message-body
     */
    protected static int maxSize = 1024 * 1024 * 50; // 50MB

    final static byte[] DOUBLE_LINE_BREAK = { 13, 10, 13, 10 };

    final static byte[] LINE_BREAK = { 13, 10 };

    final static Logger log = LoggerFactory.getLogger(MultiPartBodyImpl.class);

    private static String getBoundary(MediaType type) {
        String boundary = type.getParameters().get("boundary");

        if (boundary == null) {
            throw new RuntimeException("boundary is not set");
        }
        return boundary;
    }


    // private List<KeyValuePair<ParameterValue>> rawCollection= new
    // ArrayList<KeyValuePair<ParameterValue>>();

    private List<KeyValuePair<FormFile>> formFiles = new ArrayList<KeyValuePair<FormFile>>();

    private List<KeyValuePair<String>> formTexts = new ArrayList<KeyValuePair<String>>();

    List<String> allFieldNames = new ArrayList<String>();

    /**
     * creates a MultiPartBody by parsing a stream
     * 
     * @param rawIn
     * @param boundary
     *            the boundary-string delimiting the parts
     * @throws HandlerException
     */
    public MultiPartBodyImpl(final InputStream rawIn, final String boundary)
            throws IOException {
        rawCollection = new ArrayList<KeyValuePair<ParameterValue>>();
        DelimiterInputStream in = new DelimiterInputStream(rawIn);
        ByteArrayOutputStream delimiterBaos = new ByteArrayOutputStream();
        delimiterBaos.write(45);// dash
        delimiterBaos.write(45);
        delimiterBaos.write(boundary.getBytes("utf-8"));
        byte[] delimiter = delimiterBaos.toByteArray();
        in.readTill(delimiter);
        in.read(); // 13
        in.read(); // 10
        delimiterBaos = new ByteArrayOutputStream();
        delimiterBaos.write(LINE_BREAK);
        delimiterBaos.write(delimiter);
        readFields(in, delimiterBaos.toByteArray());
        // from rfc 2616: a bare CR
        // or LF MUST NOT be substituted for CRLF within any of the HTTP
        // control
        // structures (such as header fields and multipart boundaries)
        // this means we are strict in expecting CRLF
    }

    /**
     * Creates a MultiPartBody from the InputStream of the body and the MediaType
     * 
     * @param rawIn
     * @param type
     * @throws HandlerException
     */
    public MultiPartBodyImpl(final InputStream rawIn, final MediaType type)
            throws IOException {
        this(rawIn, getBoundary(type));
    }


    public String[] getFileParameterNames() {
        String[] result = new String[formFiles.size()];
        int i = 0;
        for (KeyValuePair<FormFile> keyValue : formFiles) {
            result[i++] = keyValue.key;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wymiwyg.wrhapi.util.bodyparser.MultiPartBody#getFormFileParameterValues(java.lang.String)
     */
    public FormFile[] getFormFileParameterValues(String name) {
        List<FormFile> values = new ArrayList<FormFile>();
        for (KeyValuePair<FormFile> keyValue : formFiles) {
            if (keyValue.key.equals(name)) {
                values.add(keyValue.value);
            }
        }
        return values.toArray(new FormFile[values.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wymiwyg.wrhapi.util.bodyparser.MultiPartBody#getParameterNames()
     */
    public String[] getParameterNames() {
        return allFieldNames.toArray(new String[allFieldNames.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wymiwyg.wrhapi.util.bodyparser.MultiPartBody#getTextParameterNames()
     */
    public String[] getTextParameterNames() {
        String[] result = new String[formTexts.size()];
        int i = 0;
        for (KeyValuePair<String> keyValue : formTexts) {
            result[i++] = keyValue.key;
        }
        return result;
    }

    public String[] getTextParameterValues(String name) {
        List<String> values = new ArrayList<String>();
        for (KeyValuePair<String> keyValue : formTexts) {
            if (keyValue.key.equals(name)) {
                values.add(keyValue.value);
            }
        }
        return values.toArray(new String[values.size()]);
    }

    @Override
    public Iterator<KeyValuePair<ParameterValue>> iterator() {
        return rawCollection.iterator();
    }

    /**
     * @param result
     * @param disposition
     * @param string
     * @param data
     * @throws IOException
     */
    private void addFileField(Disposition disposition, String mediaTypeString,
            byte[] data) throws IOException {
        String name = disposition.getName();
            rawCollection.add(new KeyValuePair<ParameterValue>(name,
                    new FormFileImpl(disposition.getFileName(), MediaType.valueOf(
                            mediaTypeString), data)));
            formFiles.add(new KeyValuePair<FormFile>(name, new FormFileImpl(
                    disposition.getFileName(), MediaType.valueOf(
                            mediaTypeString), data)));

    }

    /**
     * @param result
     * @param disposition
     * @param data
     */
    private void addTextField(Disposition disposition, byte[] data) {
        String name = disposition.getName();
        try {
            rawCollection.add(new KeyValuePair<ParameterValue>(name,
                    new StringParameterValue(new String(data, "utf-8"))));
            formTexts.add(new KeyValuePair<String>(name, new String(data,
                    "utf-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @param in
     * @param bs
     * @return
     * @throws IOException
     * @throws IOException
     */
    private void readFields(DelimiterInputStream in, byte[] delimiter)
            throws IOException {
        while (true) {
            Map<String, String> partHeaders;
            partHeaders = readHeaders(in);

            String dispositionString = (String) partHeaders
                    .get("content-disposition");
            byte[] data;
            data = in.readTill(delimiter);

            Disposition disposition = new Disposition(dispositionString);
            allFieldNames.add(disposition.name);
            if (disposition.getFileName() != null) {
                addFileField(disposition, (String) partHeaders
                        .get(HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ENGLISH)), data);
            } else {
                addTextField(disposition, data);
            }
            byte[] twoBytes = new byte[2];
            in.read(twoBytes);
            if (twoBytes[0] == '-') {
                // the terminating two dashed (instead of CRLF
                break;
            }
        }

    }

    /**
     * 
     * This reads the headers, it stops after having read CRLFCRLF
     * The keys in the Map are lower case strings
     *
     * @param in
     * @return
     * @throws
     * @throws IOException
     */
    private Map<String, String> readHeaders(DelimiterInputStream in)
            throws IOException {
        Map<String, String> result = new HashMap<String, String>();
        for (byte[] line = in.readTill(LINE_BREAK); line.length > 0; line = in
                .readTill(LINE_BREAK)) {
            String string = new String(line, "utf-8");
            int colonPos = string.indexOf(':');
            if (colonPos == -1) {
                log.error("Header conatins no colon: " + string);
                continue;
            }
            int startPos = 0;
            while ((string.charAt(startPos) == '\n')
                    || (string.charAt(startPos) == '\r')) {
                startPos++;
            }
            String headerName = string.substring(startPos, colonPos)
                    .toLowerCase(Locale.ENGLISH);

            String value = string.substring(colonPos + 1);
            result.put(headerName, value);
        }
        return result;
    }

}