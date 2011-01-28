package org.apache.clerezza.jaxrs.utils.form;

/**
 * @author reto
 * @param <T>
 *            The type of the value
 * 
 */
public class KeyValuePair<T> {
	String key;
	T value;

	KeyValuePair(String key, T value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

}