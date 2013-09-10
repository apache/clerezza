package rdf.virtuoso.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Credit: This code is largely cloned by the thread safe test in
 * {@see org.apache.clerezza.rdf.sesame.storage.ThreadSafetyTest}
 * 
 * @author enridaga
 * 
 */
public class ThreadSafetyTest {

	private ExecutorService executor;
	private VirtuosoMGraph mgraph;

	static Logger log = LoggerFactory.getLogger(ThreadSafetyTest.class);

	@Before
	public void setUp() throws Exception {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		mgraph = new VirtuosoMGraph("ThreadSafetyTest",
				TestUtils.getConnection());
		executor = Executors.newCachedThreadPool();
	}

	@After
	public void tearDown() throws Exception {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		try {
			executor.shutdown();
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				fail("Timeout while waiting for termination");
			}
		} finally {
			mgraph.clear();
			mgraph = null;
		}
	}

	@Test
	public void testProduceFirstAndThenConsume() throws Exception {
		log.info("testProduceFirstAndThenConsume()");
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		// Produce first...
		Future<Result> fp = executor.submit(new Producer("A", 100));
		fp.get().assertResult();

		// ...and then consume
		Future<Result> fc = executor.submit(new Consumer("A", 100));
		fc.get().assertResult();
		Iterator<Triple> it = mgraph.iterator();
		while (it.hasNext()) {
			TestUtils.stamp(it.next());
		}
		// test graph size
		assertEquals(0, mgraph.size());
	}

	@Test
	public void testProduceAndConsumeSingle() throws Exception {
		log.info("testProduceAndConsumeSingle()");
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		List<Task> tasks = Arrays.asList(
				new Consumer("A", 100), new Producer("A", 100));
		List<Future<Result>> futures = executor.invokeAll(tasks);
		for (Future<Result> future : futures) {
			future.get().assertResult();
		}
		assertEquals(0, mgraph.size());
	}

	@Test
	public void testProduceAndConsumeMultiple() throws Exception {
		log.info("testProduceAndConsumeMultiple()");
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		List<Task> tasks = Arrays.asList(
				new Consumer("A", 100), new Producer("A", 100),
				new Consumer("B", 100), new Producer("B", 100),
				new Consumer("C", 100), new Producer("C", 100),
				new Consumer("D", 100), new Producer("D", 100));
		List<Future<Result>> futures = executor.invokeAll(tasks);
		for (Future<Result> future : futures) {
			future.get().assertResult();
		}
		assertEquals(0, mgraph.size());
	}

	@Test
	public void testProduceAndConsumeMixed() throws Exception {
		log.info("testProduceAndConsumeMixed()");
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		List<? extends Task> tasks = Arrays.asList(
				new Consumer("A", 110), new Consumer("A", 170),
				new Consumer("B", 100), new Consumer("B", 500),
				new Consumer("C", 230), new Consumer("C", 230),
				new Consumer("D", 540), new Consumer("D", 10),
				new Producer("D", 50), new Producer("D", 500),
				new Producer("C", 400), new Producer("C", 60),
				new Producer("B", 300), new Producer("B", 300),
				new Producer("A", 200), new Producer("A", 80));
		List<Future<Result>> futures = executor.invokeAll(tasks);
		for (Future<Result> future : futures) {
			future.get().assertResult();
		}
		assertEquals(0, mgraph.size());
	}
	/**
	 * The <code>Task</code> specifies a <code>Callable</code> whoes execution
	 * results in an <code>Integer</code>.
	 */
	private abstract class Task implements Callable<Result> {

		protected final String collectionName;
		protected final UriRef subject;
		protected final UriRef predicate;
		protected final int numElements;

		/**
		 * Creates a new <code>Task</code>.
		 * 
		 * @param collectionName
		 *            Name of the task's collection.
		 * @param numElements
		 *            The number of elements to process.
		 */
		protected Task(String collectionName, int numElements) {
			if (collectionName == null) {
				throw new IllegalArgumentException("Invalid name: null");
			} else if (collectionName.length() <= 0) {
				throw new IllegalArgumentException("Invalid name: '"
						+ collectionName + "'");
			} else if (numElements < 0) {
				throw new IllegalArgumentException("Invalid numElements: "
						+ numElements);
			}
			this.numElements = numElements;
			this.collectionName = collectionName;
			subject = new UriRef("http://example.org/" + "collection/"
					+ collectionName);
			predicate = new UriRef("http://example.org/ontology/contains");
		}

		/**
		 * Causes the currently executing thread object to temporarily pause and
		 * allow other threads to execute.
		 */
		protected void yield() {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * This class represents a task that produces triples that are stored in the
	 * graph.
	 */
	private class Producer extends Task {

		/**
		 * Creates a new <code>Producer</code> task.
		 * 
		 * @param collectionName
		 *            Name of the task's collection.
		 * @param numElements
		 *            The number of elements to produce.
		 */
		public Producer(String collectionName, int numElements) {
			super(collectionName, numElements);
		}

		@Override
		public Result call() throws InterruptedException {
			int counter = 0;
			for (int elementName = 1; counter < numElements; elementName++) {
				yield();
				final Triple t = createTriple(elementName);
				if (mgraph.add(t)) {
					counter++;
					yield();
				} else {
					System.out.println("WARNING: element " + t + "NOT created");
				}
			}
			return new Result(collectionName, "Produced elements", numElements,
					counter);
		}

		/**
		 * Creates a new collection element triple.
		 * 
		 * @param value
		 *            Value of the collection element.
		 * @return A new triple representing the collection element.
		 */
		protected Triple createTriple(int value) {
			final UriRef object = new UriRef("http://example.org/"
					+ collectionName + "/" + Integer.valueOf(value)
					+ Math.random());
			return new TripleImpl(subject, predicate, object);
		}
	}

	/**
	 * This class represents a task that produces triples that are stored in the
	 * graph.
	 */
	private class Consumer extends Task {

		/**
		 * Creates a new <code>Consumer</code> task.
		 * 
		 * @param collectionName
		 *            Name of the task's collection.
		 * @param numElements
		 *            The number of elements to consume.
		 */
		public Consumer(String collectionName, int numElements) {
			super(collectionName, numElements);
		}

		/**
		 * Performs this task.
		 * 
		 * @return the number of elements successfully added to the graph.
		 */
		@Override
		public Result call() throws InterruptedException {
			int counter = 0;
			while (counter < numElements) {
				yield();

				Triple triple = null;
				mgraph.getLock().writeLock().lock();
				try {
					// System.out.println("synchonized");
					Iterator<Triple> i = mgraph
							.filter(subject, predicate, null);
					if (i.hasNext()) {
						triple = i.next();
					}

					if (triple != null && mgraph.remove(triple)) {
						counter++;
					}
				} finally {
					mgraph.getLock().writeLock().unlock();
				}
			}
			return new Result(collectionName, "Consumed elements", numElements,
					counter);
		}
	}

	/**
	 * Task result that asserts the number of processed elements.
	 */
	private class Result {

		private final int expected;
		private final int actual;
		private final String msg;
		private final String cn;

		/**
		 * Creates a new task result that asserts the element count.
		 * 
		 * @param cn
		 *            Name of the affected collection.
		 * @param msg
		 *            Assertion message to print.
		 * @param expected
		 *            Expected number of processed elements.
		 * @param actual
		 *            Actual number of processed elements.
		 */
		public Result(String cn, String msg, int expected, int actual) {
			this.expected = expected;
			this.actual = actual;
			this.msg = msg;
			this.cn = cn;
		}

		/**
		 * Asserts this result.
		 */
		public void assertResult() {
			assertEquals("[" + cn + "] " + msg, expected, actual);
		}
	}
}
