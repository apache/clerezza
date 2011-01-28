package org.apache.clerezza.jaxrs.utils.form;

/**
 * @author reto
 * 
 */
public class StringParameterValue implements ParameterValue {

	private String value;

	/**
	 * @param value
	 *            the value of this parameter
	 */
	public StringParameterValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

}