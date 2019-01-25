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

package com.liferay.portal.search.aggregation.metrics;

import com.liferay.portal.search.aggregation.AggregationVisitor;
import com.liferay.portal.search.aggregation.BaseAggregation;
import com.liferay.portal.search.sort.Sort;

/**
 * @author Rafael Praxedes
 */
public class TopHitsAggregation extends BaseAggregation {
	
	public TopHitsAggregation(String aggregationName) {
		super(aggregationName);
	}

	@Override
	public <T> T accept(AggregationVisitor<T> aggregationVisitor) {
		return aggregationVisitor.visit(this);
	}
	
	public int getFrom() {
		return _from;
	}

	public void setFrom(int from) {
		_from = from;
	}

	public int getSize() {
		return _size;
	}

	public void setSize(int size) {
		_size = size;
	}

	public Sort[] getSorts() {
		return _sorts;
	}

	public void setSorts(Sort[] sorts) {
		_sorts = sorts;
	}

	private int _from;
	private int _size;
	private Sort[] _sorts = {};
}