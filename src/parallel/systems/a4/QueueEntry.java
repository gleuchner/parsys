package parallel.systems.a4;

public class QueueEntry<T extends Comparable<? super T>> implements Comparable<QueueEntry<T>> {

	T _element;

	long _time;

	public QueueEntry(T element) {
		_time = System.currentTimeMillis();
		_element = element;

	}

	public int compareTo(QueueEntry<T> t) {
		int tc = this._element.compareTo(t._element);
		if (tc != 0) {
			return tc;
		} else {
			if (this._time < t._time) {
				return -1;
			} else if (this._time > t._time) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public T getElement() {
		return _element;
	}

	@Override
	public String toString() {
		return _time + ": " + _element.toString();
	}
}
