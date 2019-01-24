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

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.search.aggregation.AggregationVisitor;

/**
 * @author Inácio Nery
 */
@ProviderType
public class DateHistogramAggregation extends BaseBucketAggregation {

	public DateHistogramAggregation(String aggregationName) {
		super(aggregationName);
	}

	@Override
	public <T> T accept(AggregationVisitor<T> aggregationVisitor) {
		return aggregationVisitor.visit(this);
	}

	public String getDateHistogramInterval() {
		return _dateHistogramInterval;
	}

	public Boolean getKeyed() {
		return _keyed;
	}

	public Long getMinDocCount() {
		return _minDocCount;
	}

	public Long getOffset() {
		return _offset;
	}

	public void setDateHistogramInterval(String dateHistogramInterval) {
		_dateHistogramInterval = dateHistogramInterval;
	}

	public void setKeyed(Boolean keyed) {
		_keyed = keyed;
	}

	public void setMinDocCount(Long minDocCount) {
		_minDocCount = minDocCount;
	}

	public void setOffset(Long offset) {
		_offset = offset;
	}

	private String _dateHistogramInterval;
	private Boolean _keyed;
	private Long _minDocCount;
	private Long _offset;

}