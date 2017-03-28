package no.artorp.profilio.javafx;

/**
 * Holds two objects, a key and a value
 * 
 * @param <K> Key object
 * @param <V> Value object
 */
public class KeyValuePair<K, V> {
	
	private K key;
	private V value;

	public KeyValuePair(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public K getKey() {
		return key;
	}
	
	public V getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}

}
