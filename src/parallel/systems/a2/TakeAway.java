package parallel.systems.a2;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TakeAway {

	private static final int INTERVAL = 5;

	private static final int TIME_MIN = 4;

	private static final int TIME_MAX = 7;

	private static final int SIZE_MIN = 2;

	private static final int SIZE_MAX = 5;

	private static final int NUM_EMPLOYEES = 3;

	private static final int NUM_GROUPS = 20;

	private Lock _lock = new ReentrantLock();

	private Condition _condition = _lock.newCondition();

	private int groupsLeft;

	private Queue<Group> _queue = new LinkedList<Group>();

	public static void main(String args[]) {
		System.out.println("Group size: " + SIZE_MIN + "-" + SIZE_MAX);
		System.out.println("Group count: " + NUM_GROUPS);
		System.out.println("Group interval: " + INTERVAL);
		System.out.println("Preparation time: " + TIME_MIN + "-" + TIME_MAX);
		System.out.println("Employee count: " + NUM_EMPLOYEES);

		final TakeAway takeaway = new TakeAway();

		for (int i = 0; i < NUM_EMPLOYEES; i++) {
			Thread t = new Thread(() -> takeaway.serve(), "Employee-" + i);
			t.start();
		}

		for (int i = 0; i < NUM_GROUPS; i++) {
			Thread t = new Thread(new Group(ThreadLocalRandom.current().nextInt(SIZE_MIN, SIZE_MAX + 1), takeaway),
					"Group-" + i);
			t.start();
			try {
				Thread.sleep(INTERVAL * 1000);
			} catch (InterruptedException e) {
			}
		}
	}

	public boolean finished() {
		_lock.lock();
		try {
			return groupsLeft == NUM_GROUPS;
		} finally {
			_lock.unlock();
		}

	}

	public void order(Group g) {
		_lock.lock();
		try {
			for (int i = 0; i < g.getMembers(); i++) {
				_queue.add(g);
			}
			System.out.println(Thread.currentThread().getName() + " ordered " + g.getMembers() + " doeners");
			System.out.println(this._queue.size() + " in queue");
			if (_queue.size() == g.getMembers()) {
				_condition.signalAll();
			}
		} finally {
			_lock.unlock();
		}
	}

	public void serve() {
		Group g = null;

		while (!this.finished()) {
			_lock.lock();
			try {
				g = _queue.poll();
				if (g == null) {
					try {
						System.out.println(Thread.currentThread().getName() + " waiting");
						_condition.await();
					} catch (InterruptedException e) {
					}
				} else {
					System.out.println(Thread.currentThread().getName() + " serving customer");
					System.out.println(_queue.size() + " in queue");
				}
			} finally {
				_lock.unlock();
			}
			if (g != null) {
				int time = ThreadLocalRandom.current().nextInt(TIME_MIN, TIME_MAX + 1);
				try {
					Thread.sleep(time * 1000);
				} catch (InterruptedException e) {
				}
				g.getLock().lock();
				try {
					System.out.println(Thread.currentThread().getName() + " prepared doener in " + time + "s");
					g.receive(1);
					g.getCondition().signal();
				} finally {
					g.getLock().unlock();
				}
			}
		}
	}

	public void leave(Group g) {
		_lock.lock();
		try {
		System.out.println(Thread.currentThread().getName() + " leaving with " + g.getServed() + "/" + g.getMembers());
		System.out.println(this._queue.size() + " in queue");
		groupsLeft++;
		if (_queue.size() == 0) {
			_condition.signalAll();
		}
		} finally {
			_lock.unlock();
		}
	}
}
