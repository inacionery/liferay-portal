package com.liferay.portal.search.sort;

import com.liferay.portal.kernel.util.StringBundler;

import java.io.Serializable;

/**
 * @author Bruno Farache
 */
public class Sort implements Serializable {

	public Sort() {
	}

	public Sort(String fieldName, boolean asc) {
		_fieldName = fieldName;
		_asc = asc;
	}

	public String getFieldName() {
		return _fieldName;
	}

	public boolean isAsc() {
		return _asc;
	}

	public void setFieldName(String fieldName) {
		_fieldName = fieldName;
	}

	public void setAsc(boolean asc) {
		_asc = asc;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(5);

		sb.append("{fieldName=");
		sb.append(_fieldName);
		sb.append(", asc=");
		sb.append(_asc);
		sb.append("}");

		return sb.toString();
	}

	private String _fieldName;
	private boolean _asc;

}