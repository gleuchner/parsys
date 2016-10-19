package parallel.systems.a1;

import java.util.concurrent.ThreadLocalRandom;

public class TakeAway {

	private static final int INTERVAL = 5;

	private static final int TIME_MIN = 4;

	private static final int TIME_MAX = 7;

	private static final int SIZE_MIN = 2;

	private static final int SIZE_MAX = 5;

	private static final int NUM_EMPLOYEES = 3;

	private static final int NUM_GROUPS = 20;

	private int _orderedDoeners = 0;

	private int _preparedDoeners = 0;

	// public Queue<Group> _queue = new LinkedList<Group>();

	public static void main(String args[]) {
		final TakeAway takeaway = new TakeAway();
		for (int i = 0; i < NUM_EMPLOYEES; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						takeaway.serve();
					}
				}
			}, "Employee-" + i).start();
		}

		for (int i = 0; i < NUM_GROUPS; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					int size = ThreadLocalRandom.current().nextInt(SIZE_MIN, SIZE_MAX + 1);
					System.out.println(Thread.currentThread().getName() + " entering with " + size + " pupils");
					takeaway.order(size);
				}
			}, "Group-" + i).start();
			try {
				Thread.sleep(INTERVAL * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void order(int amount) {
		System.out.println(Thread.currentThread().getName() + " ordered " + amount + " doeners");
		_orderedDoeners += amount;
		while (_preparedDoeners < amount) {
			try {
				this.notify();
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		_preparedDoeners -= amount;
		System.out.println(Thread.currentThread().getName() + " leaving");
	}

	public synchronized void serve() {
		while (true) {
			if (_orderedDoeners > 0) {
				try {
					int time = ThreadLocalRandom.current().nextInt(TIME_MIN, TIME_MAX + 1);
					System.out.println(Thread.currentThread().getName() + " preparing doener in " + time + "s");
					_orderedDoeners--;
					_preparedDoeners++;
					System.out.println("Ordered: " + _orderedDoeners + "\tPrepared: " + _preparedDoeners);
					Thread.sleep(time * 1000);
					this.notify();
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
