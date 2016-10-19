package parallel.systems.a1;

import java.util.concurrent.ThreadLocalRandom;

public class TakeAway {

	private static final int INTERVAL = 5;

	private static final int TIME_MIN = 1;

	private static final int TIME_MAX = 4;

	private static final int SIZE_MIN = 1;

	private static final int SIZE_MAX = 10;

	private static final int NUM_EMPLOYEES = 3;

	private static final int NUM_GROUPS = 20;
	
	private int groupsLeft;

	private int _orderedDoeners = 0;

	private int _preparedDoeners = 0;

	public static void main(String args[]) {
		final TakeAway takeaway = new TakeAway();
		for (int i = 0; i < NUM_EMPLOYEES; i++) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					while (!takeaway.finished()) {
						takeaway.serve();
					}
				}
			}, "Employee-" + i);
			t.start();
		}

		for (int i = 0; i < NUM_GROUPS; i++) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					int size = ThreadLocalRandom.current().nextInt(SIZE_MIN, SIZE_MAX + 1);

					System.out.println(Thread.currentThread().getName() + " entering with " + size + " pupils");

					takeaway.order(size);
					takeaway.waitForOrder(size);
				}
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

	public synchronized void order(int amount) {
		_orderedDoeners += amount;

		System.out.println(Thread.currentThread().getName() + " ordered " + amount + " doeners");
		System.out.println("Ordered: " + _orderedDoeners + "\tPrepared: " + _preparedDoeners);

		this.notify();
	}

	public synchronized void waitForOrder(int amount) {
		while (_preparedDoeners < amount) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		_preparedDoeners -= amount;

		System.out.println(Thread.currentThread().getName() + " leaving");
		System.out.println("Ordered: " + _orderedDoeners + "\tPrepared: " + _preparedDoeners);
		
		groupsLeft++;
	}

	public synchronized void serve() {
		if (_orderedDoeners > 0) {
			int time = ThreadLocalRandom.current().nextInt(TIME_MIN, TIME_MAX + 1);

			_orderedDoeners--;
			_preparedDoeners++;

			System.out.println(Thread.currentThread().getName() + " preparing doener in " + time + "s");
			System.out.println("Ordered: " + _orderedDoeners + "\tPrepared: " + _preparedDoeners);
			try {
				Thread.sleep(time * 1000);
			} catch (InterruptedException e) {
			}
			this.notify();
		}
	}
}
