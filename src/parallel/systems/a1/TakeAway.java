package parallel.systems.a1;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class TakeAway {

	private static final int INTERVAL = 5;

	private static final int TIME_MIN = 4;

	private static final int TIME_MAX = 7;

	private static final int SIZE_MIN = 2;

	private static final int SIZE_MAX = 5;

	private static final int NUM_EMPLOYEES = 3;

	private static final int NUM_GROUPS = 20;

	private int groupsLeft;

	private Queue<Group> _queue = new LinkedList<Group>();

	public static void main(String args[]) {
		final TakeAway takeaway = new TakeAway();
		for (int i = 0; i < NUM_EMPLOYEES; i++) {
			Thread t = new Thread(() -> takeaway.serve(), "Employee-" + i);
			t.start();
		}

		for (int i = 0; i < NUM_GROUPS; i++) {
			Thread t = new Thread(() -> {
				Group group = new Group(ThreadLocalRandom.current().nextInt(SIZE_MIN, SIZE_MAX + 1));

				System.out
						.println(Thread.currentThread().getName() + " entering with " + group.getMembers() + " pupils");

				takeaway.order(group);
				takeaway.waitForOrder(group);
			}, "Group-" + i);
			t.start();
			try {
				Thread.sleep(INTERVAL * 1000);
			} catch (InterruptedException e) {
			}
		}
	}

	public synchronized boolean finished() {
		return groupsLeft == NUM_GROUPS;
	}

	public void order(Group g) {
		synchronized (this) {
			for (int i = 0; i < g.getMembers(); i++) {
				_queue.add(g);
			}
			System.out.println(Thread.currentThread().getName() + " ordered " + g.getMembers() + " doeners");
			System.out.println(this._queue.size() + " in queue");
			if (_queue.size() == g.getMembers()) {
				this.notifyAll();
			}
		}
	}

	public void waitForOrder(Group g) {
		synchronized (g) {
			while (g.getServed() < g.getMembers()) {
				try {
					g.wait();
				} catch (InterruptedException e) {
				}
			}
			synchronized (this) {
				System.out.println(
						Thread.currentThread().getName() + " leaving with " + g.getServed() + "/" + g.getMembers());
				System.out.println(this._queue.size() + " in queue");
				groupsLeft++;
				if (_queue.size() == 0) {
					this.notifyAll();
				}
			}
		}
	}

	public void serve() {
		Group g = null;

		while (!this.finished()) {
			synchronized (this) {
				g = _queue.poll();
				if (g == null) {
					try {
						System.out.println(Thread.currentThread().getName() + " waiting");
						this.wait();
					} catch (InterruptedException e) {
					}
				} else {
					System.out.println(Thread.currentThread().getName() + " serving customer");
					System.out.println(_queue.size() + " in queue");
				}
			}
			if (g != null) {
				int time = ThreadLocalRandom.current().nextInt(TIME_MIN, TIME_MAX + 1);
				try {
					Thread.sleep(time * 1000);
				} catch (InterruptedException e) {
				}
				synchronized (g) {
					System.out.println(Thread.currentThread().getName() + " prepared doener in " + time + "s");
					g.receive(1);
					g.notify();
				}
			}
		}
	}
}
