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
package org.apache.clerezza.rdf.locking;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.debug.ReadLockDebug;
import org.apache.clerezza.rdf.core.access.debug.ReentrantReadWriteLockTracker;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;

/**
 * Provides an overview page of all locks of all graphs.
 * @author mir
 */
@Component()
@Services({
	@Service(value=Object.class),
	@Service(value=GlobalMenuItemsProvider.class)
})
@Property(name = "javax.ws.rs", boolValue = true)
@Path("admin/graphs/locks")
public class LockOverview implements GlobalMenuItemsProvider {

	@Reference
	TcManager tcManager;

	@GET
	public String getOverview() {
		Set<Thread> threadSet = getAllThreads();
		Iterator<UriRef> mGraphUris = tcManager.listMGraphs().iterator();

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		while (mGraphUris.hasNext()) {
			UriRef uriRef = mGraphUris.next();
			ReentrantReadWriteLock lock = (ReentrantReadWriteLock) tcManager.getMGraph(uriRef).getLock();
			int readLockCount = lock.getReadLockCount();
			printWriter.append(uriRef.getUnicodeString());
			printWriter.append("\n");
			printWriter.append("Read-Lock count:    " + readLockCount);
			printWriter.append("\n");
			printWriter.append("Write-Locked:       " + (lock.isWriteLocked() ? "YES" : "NO"));
			printWriter.append("\n");
			printWriter.append("Has queued threads: " + (lock.hasQueuedThreads() ? "YES" : "NO"));
			printWriter.append("\n");
			if (readLockCount > 0 && lock instanceof ReentrantReadWriteLockTracker) {
				ReentrantReadWriteLockTracker debugLock = (ReentrantReadWriteLockTracker) lock;
				printWriter.append("Threads holding read-lock: \n");
				Set<ReadLockDebug> lockedReadLocks = debugLock.getLockedReadLocks();
				for (ReadLockDebug readLockDebug : lockedReadLocks) {
					printStackTrace(readLockDebug.getStackTrace(), printWriter);
					printWriter.append("\n");
				}
				printWriter.append("\n");
			}
			if (lock.isWriteLocked() && lock instanceof ReentrantReadWriteLockTracker) {
				ReentrantReadWriteLockTracker debugLock = (ReentrantReadWriteLockTracker) lock;
				printWriter.append("Thread holding write-lock: \n");
				printStackTrace(debugLock.writeLock().getStackTrace(), printWriter);
				printWriter.append("\n");
			}
			printWriter.append("Queue length:       " + lock.getQueueLength());
			printWriter.append("\n");
			printWriter.append("Queued threads: ");
			printWriter.append("\n");
			printQueuedThreads(lock, printWriter, threadSet);
			printWriter.append("----------------------------------------------------");
			printWriter.append("\n");

		}
		return stringWriter.toString();
	}

	private void printQueuedThreads(ReentrantReadWriteLock lock, PrintWriter printWriter,
			Set<Thread> threadSet) {
		for (Thread thread : threadSet) {
			if (lock.hasQueuedThread(thread)) {
				printWriter.append("" + thread.getId());
				printStackTrace(thread.getStackTrace(), printWriter);
				printWriter.append("\n");
			}
		}
	}

	private void printStackTrace(StackTraceElement[] stackTrace, PrintWriter printWriter) {
		Throwable throwable = new Throwable();
		throwable.setStackTrace(stackTrace);
		throwable.printStackTrace(printWriter);
	}

	public Set<Thread> getAllThreads() {
		// Find the root thread group
		ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
		while (root.getParent() != null) {
			root = root.getParent();
		}
		HashSet<Thread> threadSet = new HashSet<Thread>();
		visit(root, 0, threadSet);
		return threadSet;
	}

	public void visit(ThreadGroup group, int level, HashSet<Thread> threadSet) {
		// Get threads in `group'
		int numThreads = group.activeCount();
		Thread[] threads = new Thread[numThreads * 2];
		numThreads = group.enumerate(threads, false);
		// Enumerate each thread in `group'
		for (int i = 0; i < numThreads; i++) {
			// Get thread
			Thread thread = threads[i];
			threadSet.add(thread);
		}
		// Get thread subgroups of `group'
		int numGroups = group.activeGroupCount();
		ThreadGroup[] groups = new ThreadGroup[numGroups * 2];
		numGroups = group.enumerate(groups, false);
		// Recursively visit each subgroup
		for (int i = 0; i < numGroups; i++) {
			visit(groups[i], level + 1, threadSet);
		}
	}

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();
		try {
			AccessController.checkPermission(
					new LockOverviewPermission());
		} catch (AccessControlException e) {
			return items;
		}
		items.add(new GlobalMenuItem("/admin/graphs/locks", "SCM", "MGraph Locking", 1,
				"Development"));
		return items;
	}
}
