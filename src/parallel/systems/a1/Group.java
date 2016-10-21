package parallel.systems.a1;

public class Group implements Runnable {

	private int _members;

	private int _served;

	private TakeAway _takeAway;

	public Group(int members, TakeAway takeAway) {
		_takeAway = takeAway;
		_members = members;
	}
	
	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName() + " entering with " + _members + " pupils");
		_takeAway.order(this);
		waitForOrder();
		_takeAway.leave(this);
	}

	public synchronized void waitForOrder() {
		while (_served < _members) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
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
}
