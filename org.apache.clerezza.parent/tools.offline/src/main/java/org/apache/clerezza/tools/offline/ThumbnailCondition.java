/*
 *  Copyright 2010 mir.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.clerezza.tools.offline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.clerezza.tools.offline.utils.StreamCondition;
import org.apache.clerezza.platform.content.representations.core.ThumbnailService;
import org.apache.clerezza.rdf.core.UriRef;

/**
 *
 * @author mir
 */
public class ThumbnailCondition implements StreamCondition {

	private final static byte[] SRC_BYTES = "src=\"".getBytes();
	private final static byte[] HREF_BYTES = "href=\"".getBytes();
	private final static byte QUOTE_BYTE = "\"".getBytes()[0];
	private final static byte[] THUMBNAIL_SERVICE_BYTES = "/thumbnail-service?".getBytes();
	private ThumbnailService thumbnailService;
	private boolean isScr = true;
	private boolean isHref = true;
	private boolean isSatisfied = false;
	private byte[] thumbnailBytes = null;
	private ByteArrayOutputStream cachedQueryParams = new ByteArrayOutputStream();

	private enum Phase {CHECK_TAG_ATTRIBUTE, CHECK_THUMBNAIL_SERVICE, LOOK_FOR_QUOTE};
	private Phase currentPhase = Phase.CHECK_TAG_ATTRIBUTE;
	private int arrayPosition = 0;

	public ThumbnailCondition(ThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	private void reset() {
		isScr = true;
		isHref = true;
		isSatisfied = false;
		arrayPosition = 0;
		cachedQueryParams.reset();
		thumbnailBytes = null;
		currentPhase = Phase.CHECK_TAG_ATTRIBUTE;
	}

	@Override
	public boolean feed(int b) {
		if (isSatisfied) {
			reset();
		}
		boolean result = false;
		if (currentPhase.equals(Phase.CHECK_TAG_ATTRIBUTE)) {
			result = checkTagAttribute(b);
		} else if (currentPhase.equals(Phase.CHECK_THUMBNAIL_SERVICE)) {
			result = checkThumbnailUri(b);
		} else if (currentPhase.equals(Phase.LOOK_FOR_QUOTE)) {
			result = lookForQuote(b);
		}
		return result;
	}

	private boolean checkTagAttribute(int b) {
		if (isScr) {
			if (SRC_BYTES[arrayPosition] != b) {
				isScr = false;
			} else if (SRC_BYTES.length == arrayPosition + 1) {
				currentPhase = Phase.CHECK_THUMBNAIL_SERVICE;
				arrayPosition = 0;
				return true;
			}
		}
		if (isHref) {
			if (HREF_BYTES[arrayPosition] != b) {
				isHref = false;
			} else if (HREF_BYTES.length == arrayPosition + 1) {
				currentPhase = Phase.CHECK_THUMBNAIL_SERVICE;
				arrayPosition = 0;
				return true;
			}
		}
		if (!isHref && !isScr) {
			reset();
			return false;
		}
		arrayPosition++;
		return true;
	}

	private boolean checkThumbnailUri(int b) {
		if (arrayPosition == 16) {
		}
		if (THUMBNAIL_SERVICE_BYTES[arrayPosition] != b) {
			reset();
			return false;
		} else if (THUMBNAIL_SERVICE_BYTES.length == arrayPosition + 1) {
			currentPhase = Phase.LOOK_FOR_QUOTE;
		}
		arrayPosition++;
		return true;
	}

	private boolean lookForQuote(int b) {
		if (b == QUOTE_BYTE) {
			prepareBytes();
			isSatisfied = true;
			return false;
		} else {
			cachedQueryParams.write(b);
		}
		return true;
	}

	private void prepareBytes() {
		ByteArrayOutputStream bous = new ByteArrayOutputStream();
		try {
			if (isHref) {
				bous.write(HREF_BYTES);
			} else {
				bous.write(SRC_BYTES);
			}
			bous.write(getThumbnailUri());
			bous.write(QUOTE_BYTE);
			thumbnailBytes = bous.toByteArray();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private byte[] getThumbnailUri() {
		ThumbnailServiceParams params = parseThumbnailServiceParams();
		UriRef thumbnailUri = thumbnailService.getThumbnailUri(params.getUri(),
				params.getWidth(), params.getHeight(), params.getExact());
		return thumbnailUri.getUnicodeString().getBytes();
	}

	@Override
	public boolean isSatisfied() {
		return isSatisfied;
	}

	@Override
	public byte[] getBytes() {
		return thumbnailBytes;
	}

	private ThumbnailServiceParams parseThumbnailServiceParams() {
		Integer width = null, height = null;
		UriRef uri = null;
		boolean extact = false;
		String queryParams = cachedQueryParams.toString();
		queryParams = queryParams.replace("&amp;", "&");
		String[] nameValues = queryParams.split("&");
		for (String nameValue : nameValues) {
			String[] nameValuePair = nameValue.split("=");
			if (nameValuePair.length == 2) {
				String name = nameValuePair[0];
				if (name.equals("uri")) {
					uri = new UriRef(nameValuePair[1]);
				} else if (name.equals("width")) {
					width = Integer.valueOf(nameValuePair[1]);
				} else if (name.equals("height")) {
					height = Integer.valueOf(nameValuePair[1]);
				} else if (name.equals("exact")) {
					extact = Boolean.valueOf(nameValuePair[1]);
				}
			}
		}
		return new ThumbnailServiceParams(width, height, uri, extact);
	}

	private class ThumbnailServiceParams {
		 private Integer width, height;
		 private UriRef uri;
		 private boolean exact;

		public ThumbnailServiceParams(Integer width, Integer height, UriRef uri,
				boolean exact) {
			this.width = width;
			this.height = height;
			this.uri = uri;
			this.exact = exact;
		}

		public Integer getHeight() {
			return height;
		}

		public UriRef getUri() {
			return uri;
		}

		public Integer getWidth() {
			return width;
		}

		public boolean getExact() {
			return exact;
		}
	}
}
