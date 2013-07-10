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
import java.util.logging.Level;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.Version;
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
    private DirectoryReader indexReader = null;
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
        public IndexWriterWrapper(Directory directory, IndexWriterConfig conf) 
                throws CorruptIndexException, LockObtainFailedException, IOException   {
            
            super(directory, conf);
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
     * @param indexDir    the directory where the index is.
     * @param analyzer    the analyzer to use. 
     */
    public LuceneTools(Directory indexDir, Analyzer analyzer) {
        logger.info("Activating Lucene tools");
        indexDirectory = indexDir;
        if(indexDirectory instanceof FSDirectory) {
            luceneIndexDir = ((FSDirectory) indexDirectory).getDirectory();
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
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41, getAnalyzer());
            indexWriter = new IndexWriterWrapper(indexDirectory, config);
            //indexWriter.setMergeFactor(1000);
            return indexWriter;
        } catch (Exception ex) {
            throw new RuntimeException("Could not initialize IndexWriter", ex);
        } 
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
        //TODO make sure a current version is returned
        if (indexReader == null) {
            try {
                indexReader = DirectoryReader.open(indexDirectory);
                indexSearcher = new IndexSearcher(indexReader);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            
        } else {
            try {
                DirectoryReader newReader = DirectoryReader.openIfChanged(indexReader);
                if (newReader != null) {
                    final IndexReader oldReader = indexReader;
                    indexReader = newReader;
                    //would be better to use ScheduledThreadPoolExecutor
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                            try {
                                oldReader.close();
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                        
                    };
                }
                indexSearcher = new IndexSearcher(indexReader);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return indexSearcher;
    }

    /**
     * Closes the IndexSearcher
     */
    public void closeIndexSearcher() {
        try {
            if(indexSearcher != null) {
                //indexSearcher.close();
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
     * //no more, see: http://www.searchworkings.org/blog/-/blogs/simon-says%3A-optimize-is-bad-for-you
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
                            getIndexWriter().forceMerge(1, true);
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
