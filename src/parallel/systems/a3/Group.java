package parallel.systems.a3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Group implements Runnable {

	private int _members;

	private int _served;
	
	private String _name;

	private TakeAway _takeAway;

	private Lock _lock = new ReentrantLock();

	private Condition _condition = _lock.newCondition();

	public Group(int members, TakeAway takeAway, String name) {
		_takeAway = takeAway;
		_members = members;
		_name = name;
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
}
