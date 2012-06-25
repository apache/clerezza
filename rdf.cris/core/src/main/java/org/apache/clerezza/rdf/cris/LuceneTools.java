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

package org.apache.clerezza.rdf.cris;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Lucene tools such as <code>IndexWriter</code> and <code>IndexSearcher</code>.
 * 
 * @author tio
 */
public class LuceneTools {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private IndexSearcher indexSearcher = null;
	private Directory indexDirectory = null;
	private IndexWriter indexWriter = null;
	private Analyzer analyzer = null;
	private File luceneIndexDir = null;
	private boolean optimizeInProgress = false;

	/**
	 * Wraps an IndexWriter to ensure that is it closed properly.
	 */
	class IndexWriterWrapper extends IndexWriter {

		private boolean invokeClose;
		public IndexWriterWrapper(Directory directory, Analyzer analyzer, 
				boolean createDir, IndexWriter.MaxFieldLength mfl) 
				throws CorruptIndexException, LockObtainFailedException, IOException   {
			
			super(directory, analyzer, createDir, mfl);
			invokeClose = true;
		}

		@Override
		protected void finalize() throws Throwable {
			try {
				if(invokeClose) {
					logger.warn("Lucene Indexwriter not explicitly closed.");
					this.close();
				}
			} finally {
				super.finalize();
			}
		}

		@Override
		public void close() throws CorruptIndexException, IOException {
			if(!optimizeInProgress) {
				super.close();
				invokeClose = false;
			}
		}
	}
	
	/**
	 * Constructor.
	 * 
	 * @param indexDir	the directory where the index is.
	 * @param analyzer	the analyzer to use. 
	 */
	public LuceneTools(Directory indexDir, Analyzer analyzer) {
		logger.info("Activating Lucene tools");
		indexDirectory = indexDir;
		if(indexDirectory instanceof FSDirectory) {
			luceneIndexDir = ((FSDirectory) indexDirectory).getFile();
		}
		this.analyzer = analyzer;
	}

	/**
	 * Return a IndexWriter
	 */
	public IndexWriter getIndexWriter() throws RuntimeException {
		return getIndexWriter(false);
	}

	/**
	 * Return a IndexWriter
	 *
	 * @param createDir specifies the path to a directory where the index should be stored.
	 */
	public IndexWriter getIndexWriter(boolean createDir) {
		if(indexWriter != null) {
			return indexWriter;
		}
		try {
			if(luceneIndexDir != null) {
				indexDirectory = FSDirectory.open(luceneIndexDir);
			}
			indexWriter = new IndexWriterWrapper(indexDirectory, getAnalyzer(),
					createDir, IndexWriter.MaxFieldLength.UNLIMITED);
			indexWriter.setMergeFactor(1000);
			return indexWriter;
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		} 
		throw new RuntimeException("Could not initialize IndexWriter");
	}

	/**
	 * Closes the IndexWriter
	 */
	public void closeIndexWriter() {
		try {
			if(indexWriter != null && !optimizeInProgress) {
				indexWriter.close();
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} catch(AlreadyClosedException ex) {
			logger.warn("IndexWriter already closed.");
		} finally {
			if(!optimizeInProgress) {
				indexWriter = null;
			}
		}
	}

	/**
	 * Returns a IndexSearcher
	 *
	 */
	public IndexSearcher getIndexSearcher() throws RuntimeException {
		try {
			if(indexSearcher != null) {
				if (indexSearcher.getIndexReader().isCurrent() || optimizeInProgress) {
					return indexSearcher;
				} else {
					indexSearcher.close();
				}
			}
			if (IndexReader.indexExists(indexDirectory)) {
				indexSearcher = new IndexSearcher(indexDirectory, true);
				return indexSearcher;
			}
		} catch (CorruptIndexException ex) {
			logger.error(ex.getMessage());
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} 
		throw new RuntimeException("Could not initialize IndexSearcher");
	}

	/**
	 * Closes the IndexSearcher
	 */
	public void closeIndexSearcher() {
		try {
			if(indexSearcher != null) {
				indexSearcher.close();
			}
			if(indexDirectory != null) {
				indexDirectory.close();
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} finally {
			indexSearcher = null;
			indexDirectory = null;
		}
	}

	/**
	 * Returns the Analyzer;
	 *
	 * @return analyzer;
	 */
	public Analyzer getAnalyzer() {
		return this.analyzer;
	}
	
	/**
	 * Starts index optimization. Optimization is started in a separate thread.
	 * This method does not wait for optimization to finish but returns immediately 
	 * after starting the optimize thread.
	 */
	synchronized public void optimizeIndex() {
		if(optimizeInProgress) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				optimizeInProgress = true;
				long id = Thread.currentThread().getId();
				Thread.currentThread().setName("CRIS Optimize Thread[" + id + "]");
				logger.info("Starting index optimization.");
				AccessController.doPrivileged(new PrivilegedAction<Void>() {

					@Override
					public Void run() {
						try {
							getIndexWriter().optimize(true);
							commit();
							logger.info("Index optimized.");
						} catch(IOException ex) {
							logger.error(ex.getMessage());
							throw new RuntimeException("Could not optimize index.", ex);
						} catch(OutOfMemoryError ex) {
							logger.error(ex.getMessage());
						} finally {
							optimizeInProgress = false;
							closeIndexWriter();
						}
						return null;
					}
				});
			}
		}).start();
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			logger.warn("lucene search in finalized method closed");
			closeIndexSearcher();
			closeIndexWriter();
			luceneIndexDir = null;
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * Commits all pending changes to the index
	 *
	 */
	protected void commitChanges() {
		commit();
	}

	private void commit() {
		try {
			getIndexWriter().commit();
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} catch(AlreadyClosedException ex) {
			logger.warn("IndexWriter already Closed: " + ex.getMessage());
			indexWriter = null;
			getIndexWriter();
			commitChanges();
		}
	}
}
