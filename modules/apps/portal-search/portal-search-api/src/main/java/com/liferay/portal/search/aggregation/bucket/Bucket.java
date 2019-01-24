/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.aggregation.bucket;

import com.liferay.portal.search.aggregation.AggregationResults;

import java.util.Objects;

/**
 * @author Inácio Nery
 */
public class Bucket {

	public Bucket(
		AggregationResults aggregationResults, long count, String key) {

		_aggregationResults = aggregationResults;
		_count = count;
		_key = key;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Bucket)) {
			return false;
		}

		Bucket other = (Bucket)obj;

		return Objects.equals(_key, other._key);
	}

	public AggregationResults getAggregationResults() {
		return _aggregationResults;
	}

	public long getCount() {
		return _count;
	}

	public String getKey() {
		return _key;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_key);
	}

	@Override
	public String toString() {
		return "Bucket [count=" + _count + ", key=" + _key + "]";
	}

	private final AggregationResults _aggregationResults;
	private final long _count;
	private final String _key;

}