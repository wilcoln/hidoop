package hdfs;

import java.io.Serializable;

public class Pair<K, V> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9162593043568727895L;
	private K key;
	private V value;
	public Pair(K k,V v) {
		this.key = k;
		this.value = v;
	}
	public K getKey() {
		return key;
	}
	public void setKey(K key) {
		this.key = key;
	}
	public V getValue() {
		return value;
	}
	public void setValue(V value) {
		this.value = value;
	}
	public String toString() {
		return "\n     le fragment "+this.key +" placé dans le noeud "+this.value;
	}
	
}
