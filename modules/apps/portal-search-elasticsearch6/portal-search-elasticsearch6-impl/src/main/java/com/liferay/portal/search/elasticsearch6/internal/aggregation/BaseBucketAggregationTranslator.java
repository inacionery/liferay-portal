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
import com.liferay.portal.search.aggregation.bucket.BaseBucketAggregation;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;

/**
 * @author Inácio Nery
 */
public class BaseBucketAggregationTranslator {

	public ValuesSourceAggregationBuilder translate(
		ValuesSourceAggregationBuilderFactory
			valuesSourceAggregationBuilderFactory,
		BaseBucketAggregation baseBucketAggregation,
		AggregationTranslator<AggregationBuilder> aggregationTranslator) {

		ValuesSourceAggregationBuilder valuesSourceAggregationBuilder =
			valuesSourceAggregationBuilderFactory.create(baseBucketAggregation);

		valuesSourceAggregationBuilder.field(baseBucketAggregation.getField());

		for (Aggregation aggregation :
				baseBucketAggregation.getAggregations()) {

			valuesSourceAggregationBuilder.subAggregation(
				aggregationTranslator.translate(aggregation));
		}

		return valuesSourceAggregationBuilder;
	}

	public interface ValuesSourceAggregationBuilderFactory {

		public ValuesSourceAggregationBuilder create(
			BaseBucketAggregation baseBucketAggregation);

	}

}