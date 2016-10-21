package parallel.systems.a1;

public class Group {

	private int _members;
	
	private int _served;
	
	public Group(int members) {
		_members = members;
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
