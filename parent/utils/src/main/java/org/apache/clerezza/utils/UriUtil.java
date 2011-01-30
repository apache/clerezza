/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/util/URIUtil.java,v 1.27 2004/05/05 20:34:01 olegk Exp $
 * $Revision: 507321 $
 * $Date: 2007-02-14 01:10:51 +0100 (Wed, 14 Feb 2007) $
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.clerezza.utils;

import java.io.StringWriter;
import java.util.BitSet;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.util.EncodingUtils;

/**
 * The URI escape and character encoding and decoding utility.
 * It's compatible with {@link org.apache.commons.httpclient.HttpURL} rather
 * than {@link org.apache.commons.httpclient.URI}.
 *
 * @author <a href="mailto:jericho@apache.org">Sung-Gu</a>
 * @version $Revision: 507321 $ $Date: 2002/03/14 15:14:01
 */
public class UriUtil {

    // ----------------------------------------------------- Instance variables

    protected static final BitSet empty = new BitSet(1);

    // ---------------------------------------------------------- URI utilities

    /**
     * Get the basename of an URI.   It's possibly an empty string.
     *
     * @param uri a string regarded an URI
     * @return the basename string; an empty string if the path ends with slash
     */
    public static String getName(String uri) {
        if (uri == null || uri.length() == 0) { return uri; }
        String path = UriUtil.getPath(uri);
        int at = path.lastIndexOf("/");
        int to = path.length();
        return (at >= 0) ? path.substring(at + 1, to) : path;
    }


    /**
     * Get the query of an URI.
     *
     * @param uri a string regarded an URI
     * @return the query string; <code>null</code> if empty or undefined
     */
    public static String getQuery(String uri) {
        if (uri == null || uri.length() == 0) { return null; }
        // consider of net_path
        int at = uri.indexOf("//");
        int from = uri.indexOf(
            "/",
            at >= 0 ? (uri.lastIndexOf("/", at - 1) >= 0 ? 0 : at + 2) : 0
        );
        // the authority part of URI ignored
        int to = uri.length();
        // reuse the at and from variables to consider the query
        at = uri.indexOf("?", from);
        if (at >= 0) {
            from = at + 1;
        } else {
            return null;
        }
        // check the fragment
        if (uri.lastIndexOf("#") > from) {
            to = uri.lastIndexOf("#");
        }
        // get the path and query.
        return (from < 0 || from == to) ? null : uri.substring(from, to);
    }


    /**
     * Get the path of an URI.
     *
     * @param uri a string regarded an URI
     * @return the path string
     */
    public static String getPath(String uri) {
        if (uri == null) {
            return null;
        }
        // consider of net_path
        int at = uri.indexOf("//");
        int from = uri.indexOf(
            "/",
            at >= 0 ? (uri.lastIndexOf("/", at - 1) >= 0 ? 0 : at + 2) : 0
        );
        // the authority part of URI ignored
        int to = uri.length();
        // check the query
        if (uri.indexOf('?', from) != -1) {
            to = uri.indexOf('?', from);
        }
        // check the fragment
        if (uri.lastIndexOf("#") > from && uri.lastIndexOf("#") < to) {
            to = uri.lastIndexOf("#");
        }
        // get only the path.
        return (from < 0) ? (at >= 0 ? "/" : uri) : uri.substring(from, to);
    }


    /**
     * Get the path and query of an URI.
     *
     * @param uri a string regarded an URI
     * @return the path and query string
     */
    public static String getPathQuery(String uri) {
        if (uri == null) {
            return null;
        }
        // consider of net_path
        int at = uri.indexOf("//");
        int from = uri.indexOf(
            "/",
            at >= 0 ? (uri.lastIndexOf("/", at - 1) >= 0 ? 0 : at + 2) : 0
        );
        // the authority part of URI ignored
        int to = uri.length();
        // Ignore the '?' mark so to ignore the query.
        // check the fragment
        if (uri.lastIndexOf("#") > from) {
            to = uri.lastIndexOf("#");
        }
        // get the path and query.
        return (from < 0) ? (at >= 0 ? "/" : uri) : uri.substring(from, to);
    }


    /**
     * Get the path of an URI and its rest part.
     *
     * @param uri a string regarded an URI
     * @return the string from the path part
     */
    public static String getFromPath(String uri) {
        if (uri == null) {
            return null;
        }
        // consider of net_path
        int at = uri.indexOf("//");
        int from = uri.indexOf(
            "/",
            at >= 0 ? (uri.lastIndexOf("/", at - 1) >= 0 ? 0 : at + 2) : 0
        );
        // get the path and its rest.
        return (from < 0) ? (at >= 0 ? "/" : uri) : uri.substring(from);
    }

    // ----------------------------------------------------- Encoding utilities

    /**
     * Get the all escaped and encoded string with the default protocl charset.
     * It's the same function to use <code>encode(String unescaped, Bitset
     * empty, URI.getDefaultProtocolCharset())</code>.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     *
     * @throws URIException if the default protocol charset is not supported
     *
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeAll(String unescaped) throws UriException {
        return encodeAll(unescaped, Uri.getDefaultProtocolCharset());
    }


    /**
     * Get the all escaped and encoded string with a given charset.
     * It's the same function to use <code>encode(String unescaped, Bitset
     * empty, String charset)</code>.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     *
     * @throws URIException if the charset is not supported
     *
     * @see #encode
     */
    public static String encodeAll(String unescaped, String charset)
        throws UriException {

        return encode(unescaped, empty, charset);
    }

	/**
     * Get the all escaped and encoded string with a given charset.
     * If the specified string contains already escaped characters, then
	 * the '%' will not be escaped.
     *
     * @param partlyEscaped a string that may containg already escaped chararcters
     * @param charset the charset
     * @return the escaped string
     *
     * @throws URIException if the charset is not supported
     *
     * @see #encode
     */
	public static String encodePartlyEncodedPath(String partlyEscaped, String charset)
		throws UriException {
		StringWriter sw = new StringWriter();
		for (int i = 0; i < partlyEscaped.length(); i++) {
			char c = partlyEscaped.charAt(i);
			if (c == '%') {
				try {
					String hexString = partlyEscaped.substring(i + 1, i + 3);
					Integer.parseInt(hexString, 16);
					sw.append(c);
					sw.append(hexString);
					i += 2;
					continue;
				} catch (NumberFormatException ex) { //encode it
				}
			}
			sw.append(UriUtil.encodePath(String.valueOf(c), charset));
		}
		return sw.toString();
	}


    /**
     * Escape and encode a string regarded as within the authority component of
     * an URI with the default protocol charset.
     * Within the authority component, the characters ";", ":", "@", "?", and
     * "/" are reserved.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     *
     * @throws URIException if the default protocol charset is not supported
     *
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeWithinAuthority(String unescaped)
        throws UriException {

        return encodeWithinAuthority(unescaped, Uri.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as within the authority component of
     * an URI with a given charset.
     * Within the authority component, the characters ";", ":", "@", "?", and
     * "/" are reserved.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     *
     * @throws URIException if the charset is not supported
     *
     * @see #encode
     */
    public static String encodeWithinAuthority(String unescaped, String charset)
        throws UriException {

        return encode(unescaped, Uri.allowed_within_authority, charset);
    }


    /**
     * Escape and encode a string regarded as the path and query components of
     * an URI with the default protocol charset.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     *
     * @throws URIException if the default protocol charset is not supported
     *
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodePathQuery(String unescaped) throws UriException {
        return encodePathQuery(unescaped, Uri.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as the path and query components of
     * an URI with a given charset.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     *
     * @throws URIException if the charset is not supported
     *
     * @see #encode
     */
    public static String encodePathQuery(String unescaped, String charset)
        throws UriException {

        int at = unescaped.indexOf('?');
        if (at < 0) {
            return encode(unescaped, Uri.allowed_abs_path, charset);
        }
        // else
        return  encode(unescaped.substring(0, at), Uri.allowed_abs_path, charset)
            + '?' + encode(unescaped.substring(at + 1), Uri.allowed_query, charset);
    }


    /**
     * Escape and encode a string regarded as within the path component of an
     * URI with the default protocol charset.
     * The path may consist of a sequence of path segments separated by a
     * single slash "/" character.  Within a path segment, the characters
     * "/", ";", "=", and "?" are reserved.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     *
     * @throws URIException if the default protocol charset is not supported
     *
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeWithinPath(String unescaped)
        throws UriException {

        return encodeWithinPath(unescaped, Uri.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as within the path component of an
     * URI with a given charset.
     * The path may consist of a sequence of path segments separated by a
     * single slash "/" character.  Within a path segment, the characters
     * "/", ";", "=", and "?" are reserved.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     *
     * @throws URIException if the charset is not supported
     *
     * @see #encode
     */
    public static String encodeWithinPath(String unescaped, String charset)
        throws UriException {

        return encode(unescaped, Uri.allowed_within_path, charset);
    }


    /**
     * Escape and encode a string regarded as the path component of an URI with
     * the default protocol charset.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     *
     * @throws URIException if the default protocol charset is not supported
     *
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodePath(String unescaped) throws UriException {
        return encodePath(unescaped, Uri.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as the path component of an URI with
     * a given charset.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     *
     * @throws URIException if the charset is not supported
     *
     * @see #encode
     */
    public static String encodePath(String unescaped, String charset)
        throws UriException {

        return encode(unescaped, Uri.allowed_abs_path, charset);
    }


    /**
     * Escape and encode a string regarded as within the query component of an
     * URI with the default protocol charset.
     * When a query comprise the name and value pairs, it is used in order
     * to encode each name and value string.  The reserved special characters
     * within a query component are being included in encoding the query.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     *
     * @throws URIException if the default protocol charset is not supported
     *
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeWithinQuery(String unescaped)
        throws UriException {

        return encodeWithinQuery(unescaped, Uri.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as within the query component of an
     * URI with a given charset.
     * When a query comprise the name and value pairs, it is used in order
     * to encode each name and value string.  The reserved special characters
     * within a query component are being included in encoding the query.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     *
     * @throws URIException if the charset is not supported
     *
     * @see #encode
     */
    public static String encodeWithinQuery(String unescaped, String charset)
        throws UriException {

        return encode(unescaped, Uri.allowed_within_query, charset);
    }


    /**
     * Escape and encode a string regarded as the query component of an URI with
     * the default protocol charset.
     * When a query string is not misunderstood the reserved special characters
     * ("&amp;", "=", "+", ",", and "$") within a query component, this method
     * is recommended to use in encoding the whole query.
     *
     * @param unescaped an unescaped string
     * @return the escaped string
     *
     * @throws URIException if the default protocol charset is not supported
     *
     * @see URI#getDefaultProtocolCharset
     * @see #encode
     */
    public static String encodeQuery(String unescaped) throws UriException {
        return encodeQuery(unescaped, Uri.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a string regarded as the query component of an URI with
     * a given charset.
     * When a query string is not misunderstood the reserved special characters
     * ("&amp;", "=", "+", ",", and "$") within a query component, this method
     * is recommended to use in encoding the whole query.
     *
     * @param unescaped an unescaped string
     * @param charset the charset
     * @return the escaped string
     *
     * @throws URIException if the charset is not supported
     *
     * @see #encode
     */
    public static String encodeQuery(String unescaped, String charset)
        throws UriException {

        return encode(unescaped, Uri.allowed_query, charset);
    }


    /**
     * Escape and encode a given string with allowed characters not to be
     * escaped and the default protocol charset.
     *
     * @param unescaped a string
     * @param allowed allowed characters not to be escaped
     * @return the escaped string
     *
     * @throws URIException if the default protocol charset is not supported
     *
     * @see URI#getDefaultProtocolCharset
     */
    public static String encode(String unescaped, BitSet allowed)
        throws UriException {

        return encode(unescaped, allowed, Uri.getDefaultProtocolCharset());
    }


    /**
     * Escape and encode a given string with allowed characters not to be
     * escaped and a given charset.
     *
     * @param unescaped a string
     * @param allowed allowed characters not to be escaped
     * @param charset the charset
     * @return the escaped string
     */
    public static String encode(String unescaped, BitSet allowed,
            String charset) throws UriException {
        byte[] rawdata = URLCodec.encodeUrl(allowed,
            EncodingUtils.getBytes(unescaped, charset));
        return EncodingUtils.getAsciiString(rawdata);
    }


    /**
     * Unescape and decode a given string regarded as an escaped string with the
     * default protocol charset.
     *
     * @param escaped a string
     * @return the unescaped string
     *
     * @throws URIException if the string cannot be decoded (invalid)
     *
     * @see URI#getDefaultProtocolCharset
     */
    public static String decode(String escaped) throws UriException {
        try {
            byte[] rawdata = URLCodec.decodeUrl(EncodingUtils.getAsciiBytes(escaped));
            return EncodingUtils.getString(rawdata, Uri.getDefaultProtocolCharset());
        } catch (DecoderException e) {
            throw new UriException(e.getMessage());
        }
    }

    /**
     * Unescape and decode a given string regarded as an escaped string.
     *
     * @param escaped a string
     * @param charset the charset
     * @return the unescaped string
     *
     * @throws URIException if the charset is not supported
     *
     * @see Coder#decode
     */
    public static String decode(String escaped, String charset)
        throws UriException {

        return Coder.decode(escaped.toCharArray(), charset);
    }

    // ---------------------------------------------------------- Inner classes

    /**
     * The basic and internal utility for URI escape and character encoding and
     * decoding.
     *
     * @deprecated use org.apache.commons.codec.net.URLCodec
     */
    protected static class Coder extends Uri {

        /**
         * Escape and encode a given string with allowed characters not to be
         * escaped.
         *
         * @param unescapedComponent an unescaped component
         * @param allowed allowed characters not to be escaped
         * @param charset the charset to encode
         * @return the escaped and encoded string
         *
         * @throws URIException if the charset is not supported
         *
         * @deprecated use org.apache.commons.codec.net.URLCodec
         */
        public static char[] encode(String unescapedComponent, BitSet allowed, String charset)
            throws UriException {

            return Uri.encode(unescapedComponent, allowed, charset);
        }


        /**
         * Unescape and decode a given string.
         *
         * @param escapedComponent an being-unescaped component
         * @param charset the charset to decode
         * @return the escaped and encoded string
         *
         * @throws URIException if the charset is not supported
         *
         * @deprecated use org.apache.commons.codec.net.URLCodec
         */
        public static String decode(char[] escapedComponent, String charset)
            throws UriException {

            return Uri.decode(escapedComponent, charset);
        }


        /**
         * Verify whether a given string is escaped or not
         *
         * @param original given characters
         * @return true if the given character array is 7 bit ASCII-compatible.
         */
        public static boolean verifyEscaped(char[] original) {
            for (int i = 0; i < original.length; i++) {
                int c = original[i];
                if (c > 128) {
                    return false;
                } else if (c == '%') {
                    if (Character.digit(original[++i], 16) == -1
                        || Character.digit(original[++i], 16) == -1) {
                        return false;
                    }
                }
            }
            return true;
        }


        /**
         * Replace from a given character to given character in an array order
         * for a given string.
         *
         * @param original a given string
         * @param from a replacing character array
         * @param to a replaced character array
         * @return the replaced string
         */
        public static String replace(String original, char[] from, char[] to) {
            for (int i = from.length; i > 0; --i) {
                original = replace(original, from[i], to[i]);
            }
            return original;
        }


        /**
         * Replace from a given character to given character for a given string.
         *
         * @param original a given string
         * @param from a replacing character array
         * @param to a replaced character array
         * @return the replaced string
         */
        public static String replace(String original, char from, char to) {
            StringBuffer result = new StringBuffer(original.length());
            int at, saved = 0;
            do {
                at = original.indexOf(from);
                if (at >= 0) {
                    result.append(original.substring(0, at));
                    result.append(to);
                } else {
                    result.append(original.substring(saved));
                }
                saved = at;
            } while (at >= 0);
            return result.toString();
        }
    }

}