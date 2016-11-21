package parallel.systems.a4;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Group implements Runnable, Comparable<Group> {

	private int _members;

	private int _served;

	private String _name;

	private TakeAway _takeAway;

	private Lock _lock = new ReentrantLock();

	private Condition _condition = _lock.newCondition();

	private boolean _goldCard;

	public Group(int members, TakeAway takeAway, String name, boolean goldCard) {
		_takeAway = takeAway;
		_members = members;
		_name = name;
		_goldCard = goldCard;
	}

	@Override
	public void run() {
		System.out.println(_name + " entering with " + _members + " pupils");
		_takeAway.order(this);
		waitForOrder();
		_takeAway.leave(this);
	}

	public void waitForOrder() {
		_lock.lock();
		try {
			while (_served < _members) {
				try {
					_condition.await();
				} catch (InterruptedException e) {
				}
			}
		} finally {
			_lock.unlock();
		}
	}

	public int getServed() {
		return _served;
	}

	public void receive(int amount) {
		this._served += amount;
	}

	public int getMembers() {
		return _members;
	}

	public Lock getLock() {
		return _lock;
	}

	public Condition getCondition() {
		return _condition;
	}

	public synchronized String getName() {
		return _name;
	}

	public boolean hasGoldCard() {
		return _goldCard;
	}

	@Override
	public int compareTo(Group o) {
		if (this.hasGoldCard()) {
			if (o.hasGoldCard()) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (o.hasGoldCard()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	@Override
	public String toString() {
		return _name + (hasGoldCard() ? " GoldCard" : "");
	}
}
