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

package com.liferay.portal.search.elasticsearch6.internal.aggregation;

import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.aggregation.AggregationTranslator;
import com.liferay.portal.search.aggregation.metrics.GeoBoundsAggregation;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBoundsAggregationBuilder;

/**
 * @author Michael C. Han
 */
public class GeoBoundsAggregationTranslator {

	public GeoBoundsAggregationBuilder translate(
		GeoBoundsAggregation geoBoundsAggregation,
		AggregationTranslator<AggregationBuilder> aggregationTranslator) {

		GeoBoundsAggregationBuilder geoBoundsAggregationBuilder =
			AggregationBuilders.geoBounds(
				geoBoundsAggregation.getAggregationName());

		geoBoundsAggregationBuilder.field(geoBoundsAggregation.getField());

		if (geoBoundsAggregation.getWrapLongitude() != null) {
			geoBoundsAggregationBuilder.wrapLongitude(
				geoBoundsAggregation.getWrapLongitude());
		}

		for (Aggregation aggregation : geoBoundsAggregation.getAggregations()) {
			geoBoundsAggregationBuilder.subAggregation(
				aggregationTranslator.translate(aggregation));
		}

		return geoBoundsAggregationBuilder;
	}

}