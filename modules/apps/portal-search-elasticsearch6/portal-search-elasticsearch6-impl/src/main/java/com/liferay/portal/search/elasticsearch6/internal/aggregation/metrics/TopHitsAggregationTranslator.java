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

package com.liferay.portal.search.elasticsearch6.internal.aggregation.metrics;

import com.liferay.portal.search.aggregation.AggregationTranslator;
import com.liferay.portal.search.aggregation.metrics.TopHitsAggregation;
import com.liferay.portal.search.sort.Sort;

import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 * @author Rafael Praxedes
 */
public class TopHitsAggregationTranslator {

	public TopHitsAggregationBuilder translate(
		TopHitsAggregation topHitsAggregation,
		AggregationTranslator<BaseAggregationBuilder> aggregationTranslaton) {

		TopHitsAggregationBuilder topHitsAggregationBuilder =
			AggregationBuilders.topHits(
				topHitsAggregation.getAggregationName());

		topHitsAggregationBuilder.from(topHitsAggregation.getFrom());

		if (topHitsAggregation.getSize() > 0) {
			topHitsAggregationBuilder.size(topHitsAggregation.getSize());
		}

		for (Sort sort : topHitsAggregation.getSorts()) {
			topHitsAggregationBuilder.sort(
				sort.getFieldName(), getSortOrder(sort.isAsc()));
		}

		return topHitsAggregationBuilder;
	}

	protected SortOrder getSortOrder(boolean asc) {
		if (asc) {
			return SortOrder.ASC;
		}

		return SortOrder.DESC;
	}

}