/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License"). You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above. However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 * 
 * trialox.org (trialox AG, Switzerland) elects to include this software in this
 * distribution under the CDDL license.
 */ 

package org.apache.clerezza.triaxrs.util;

import java.text.ParseException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpHeaderReaderImpl extends HttpHeaderReader {
    private static final int TOKEN          = 0;
    private static final int QUOTED_STRING  = 1;
    private static final int COMMENT        = 2;
    private static final int SEPARATOR      = 3;
    private static final int CONTROL        = 4;
    
    private static final char[] WHITE_SPACE = { '\t', '\r', '\n', ' '};
    
    private static final char[] SEPARATORS = { '(', ')', '<', '>', '@'
                      , ',' , ';' , ':' , '\\' , '"'
                      , '/' , '[' , ']' , '?' , '='
                      , '{' , '}' , ' ' , '\t' };
            
    private static final int[] EVENT_TABLE = createEventTable();
    
    private static int[] createEventTable() {
        int[] table = new int[128];
        
        // Token
        for (int i = 0; i < 127; i++)
            table[i] = TOKEN;
        
        // Separator
        for (char c : SEPARATORS)
            table[c] = SEPARATOR;
        
        // Comment
        table['('] = COMMENT;
        
        // QuotedString
        table['"'] = QUOTED_STRING;

        // Control
        for (int i = 0; i < 32; i++)
            table[i] = CONTROL;
        table[127] = CONTROL;

        // White space
        for (char c : WHITE_SPACE)
            table[c] = -1;
        
        return table;
    }
    
    private static final boolean[] IS_WHITE_SPACE = createWhiteSpaceTable();
    
    private static boolean[] createWhiteSpaceTable() {
        boolean[] table = new boolean[128];
        
        for (char c : WHITE_SPACE)
            table[c] = true;
        
        return table;
    }
    
    private static final boolean[] IS_TOKEN = createTokenTable();
            
    private static boolean[] createTokenTable() {
        boolean[] table = new boolean[128];
    
        for (int i = 0; i < 128; i++)
            table[i] = (EVENT_TABLE[i] == TOKEN);
        
        return table;
    }    
        
    private String header;
    
    private boolean processComments;
    
    private int index;
    
    private int length;
    
    private Event event;
    
    private String value;
    
    public HttpHeaderReaderImpl(String header, boolean processComments) {
	this.header = (header == null) ? "" : header;
	this.processComments = processComments;
	this.index = 0;
	this.length = this.header.length();
    }

    public HttpHeaderReaderImpl(String header)  {
	this(header, false);
    }

    public boolean hasNext() {
        return skipWhiteSpace();
    }
    
    public boolean hasNextSeparator(char separator, boolean skipWhiteSpace) {
        if (skipWhiteSpace)
            skipWhiteSpace();
        
        if (index >= length)
            return false;
        
        char c = header.charAt(index);
        return (EVENT_TABLE[c] == SEPARATOR)
            ? c == separator : false;
    }
    
    public Event next() throws ParseException {
        return event = process(getNextCharacter(true));
    }

    public Event next(boolean skipWhiteSpace) throws ParseException {
        return event = process(getNextCharacter(skipWhiteSpace));
    }
    
    
    public Event getEvent() {
        return event;
    }
    
    public String getEventValue() {
        return value;
    }
    
    public String getRemainder() {
        return (index < length) ? header.substring(index) : null;
    }

    public int getIndex() {
        return index;
    }
    
    private boolean skipWhiteSpace() {
	for (; index < length; index++)
            if (!isWhiteSpace(header.charAt(index)))
		return true;
        
        return false;
    }

    private char getNextCharacter(boolean skipWhiteSpace) throws ParseException {
        if (skipWhiteSpace)
            skipWhiteSpace();
        
        if (index >= length)
            throw new ParseException("End of header", index);

        return header.charAt(index);
    }
    
    private Event process(char c) throws ParseException {
        if (c > 127) {
            index++;
            return Event.Control;
        }
        
        switch(EVENT_TABLE[c]) {
            case TOKEN: {
                final int start = index;
                for (index++; index < length; index++) 
                    if (!isToken(header.charAt(index)))
                        break;
                value = header.substring(start, index);
                return Event.Token;
            }
            case QUOTED_STRING:
                processQuotedString();
                return Event.QuotedString;
            case COMMENT:
                if (!processComments) 
                    throw new ParseException("Comments are not allowed", index);

                processComment();
                return Event.Comment;
            case SEPARATOR:
                index++;
                value = String.valueOf(c);
                return Event.Separator;
            case CONTROL:
                index++;
                value = String.valueOf(c);
                return Event.Control;
            default:
                // White space
                throw new ParseException("White space not allowed", index);
        }
    }
    
    private void processComment() throws ParseException {        
	boolean filter = false;
        int nesting;
        int start;
        for (start = ++index, nesting = 1; 
             nesting > 0 && index < length;
             index++) {
            char c = header.charAt(index);
            if (c == '\\') {
                index++;
                filter = true;
            } else if (c == '\r')
                filter = true;
            else if (c == '(')
                nesting++;
            else if (c == ')')
                nesting--;
        }
        if (nesting != 0)
            throw new ParseException("Unbalanced comments", index);
        
        value = (filter) 
            ? filterToken(header, start, index - 1)
            : header.substring(start, index - 1);
    }
    
    private void processQuotedString() throws ParseException {        
        boolean filter = false;
        for (int start = ++index; index < length; index++) {
            char c = this.header.charAt(index);
            if (c == '\\') {
                index++;
                filter = true;
            } else if (c == '\r')
                filter = true;
            else if (c == '"') {
                value = (filter) 
                    ? filterToken(header, start, index)
                    : header.substring(start, index);

                index++;
                return;
            }
        }
        
        throw new ParseException("Unbalanced quoted string", index);
    }
    
    private boolean isWhiteSpace(char c) {
        return (c < 128 && IS_WHITE_SPACE[c]);
    }
    
    private boolean isToken(char c) {
        return (c < 128 && IS_TOKEN[c]);
    }
    
    private static String filterToken(String s, int start, int end) {
	StringBuffer sb = new StringBuffer();
	char c;
	boolean gotEscape = false;
	boolean gotCR = false;

	for (int i = start; i < end; i++) {
	    c = s.charAt(i);
	    if (c == '\n' && gotCR) {
		// This LF is part of an unescaped 
		// CRLF sequence (i.e, LWSP). Skip it.
		gotCR = false;
		continue;
	    }

	    gotCR = false;
	    if (!gotEscape) {
		// Previous character was NOT '\'
		if (c == '\\') // skip this character
		    gotEscape = true;
		else if (c == '\r') // skip this character
		    gotCR = true;
		else // append this character
		    sb.append(c);
	    } else {
		// Previous character was '\'. So no need to 
		// bother with any special processing, just 
		// append this character
		sb.append(c);
		gotEscape = false;
	    }
	}
	return sb.toString();
    }    
}
