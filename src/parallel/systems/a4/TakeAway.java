package parallel.systems.a4;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TakeAway {

	private static final int INTERVAL = 2;

	private static final int INTERVAL_GOLD_CARD = 2 * INTERVAL;

	private static final int TIME_MIN = 2;

	private static final int TIME_MAX = 3;

	private static final int SIZE_MIN = 2;

	private static final int SIZE_MAX = 5;

	private static final int NUM_EMPLOYEES = 3;

	private static final int NUM_GROUPS = 20;

	private static ExecutorService _groupPool;

	private static ScheduledExecutorService _startPool;

	private static ExecutorService _employeePool;

	private int _groupsLeft = 0;

	private int _groupsStarted = 0;

	private int _goldGroupsStarted = 0;

	private Lock _lock = new ReentrantLock();

	private BlockingQueue<QueueEntry<Group>> _queue = new PriorityBlockingQueue<QueueEntry<Group>>();

	public static void main(String args[]) {
		System.out.println("Group size: " + SIZE_MIN + "-" + SIZE_MAX);
		System.out.println("Group count: " + NUM_GROUPS);
		System.out.println("Group interval: " + INTERVAL);
		System.out.println("Preparation time: " + TIME_MIN + "-" + TIME_MAX);
		System.out.println("Employee count: " + NUM_EMPLOYEES);

		final TakeAway takeaway = new TakeAway();

		_groupPool = Executors.newCachedThreadPool();

		_startPool = Executors.newScheduledThreadPool(1);

		_employeePool = Executors.newFixedThreadPool(NUM_EMPLOYEES, new ThreadFactory() {
			int count = 0;

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "Employee-" + count);
				count++;
				return thread;
			}
		});

		for (int i = 0; i < NUM_EMPLOYEES; i++) {
			_employeePool.submit(() -> takeaway.serve());
		}

		_startPool.scheduleAtFixedRate(() -> {
			takeaway._lock.lock();
			try {
				if (takeaway._groupsStarted < NUM_GROUPS) {
					_groupPool.submit(new Group(ThreadLocalRandom.current().nextInt(SIZE_MIN, SIZE_MAX + 1), takeaway,
							"Group-" + takeaway._groupsStarted, false));
					takeaway._groupsStarted++;
				}
			} finally {
				takeaway._lock.unlock();
			}
		}, 0, INTERVAL, TimeUnit.SECONDS);

		_startPool.scheduleAtFixedRate(() -> {
			takeaway._lock.lock();
			try {
				if (takeaway._groupsStarted < NUM_GROUPS) {
					_groupPool.submit(new Group(ThreadLocalRandom.current().nextInt(SIZE_MIN, SIZE_MAX + 1), takeaway,
							"GoldCardGroup-" + takeaway._goldGroupsStarted, true));
					takeaway._goldGroupsStarted++;
				}
			} finally {
				takeaway._lock.unlock();
			}
		}, 0, INTERVAL_GOLD_CARD, TimeUnit.SECONDS);
	}

	public boolean finished() {
		_lock.lock();
		try {
			return _groupsLeft == NUM_GROUPS + _goldGroupsStarted;
		} finally {
			_lock.unlock();
		}
	}

	public void order(Group g) {
		for (int i = 0; i < g.getMembers(); i++) {
			_queue.add(new QueueEntry<Group>(g));
		}
		System.out.println(g.getName() + " ordered " + g.getMembers() + " doeners");
		System.out.println(this._queue.size() + " in queue");
	}

	public void serve() {
		Group g;

		while (!this.finished()) {
			try {
				g = _queue.take().getElement();

				int time = ThreadLocalRandom.current().nextInt(TIME_MIN, TIME_MAX + 1);
				try {
					Thread.sleep(time * 1000);
				} catch (InterruptedException e) {
				}
				if (g != null) {
					g.getLock().lock();
					try {
						System.out.println(Thread.currentThread().getName() + " prepared doener in " + time + "s");
						g.receive(1);
						g.getCondition().signal();
					} finally {
						g.getLock().unlock();
					}
				}
			} catch (InterruptedException e1) {
			}
		}
	}

	public void leave(Group g) {
		_lock.lock();
		try {
			System.out.println(g.getName() + " leaving with " + g.getServed() + "/" + g.getMembers());
			System.out.println(this._queue.size() + " in queue");
			_groupsLeft++;
			if (_groupsLeft == NUM_GROUPS + _goldGroupsStarted) {
				_startPool.shutdown();
				_groupPool.shutdown();
				_employeePool.shutdownNow();
			}
		} finally {
			_lock.unlock();
		}
	}
}
