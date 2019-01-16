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
import com.liferay.portal.search.aggregation.AggregationVisitor;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.aggregation.metrics.AvgAggregation;
import com.liferay.portal.search.aggregation.metrics.CardinalityAggregation;
import com.liferay.portal.search.aggregation.metrics.ExtendedStatsAggregation;
import com.liferay.portal.search.aggregation.metrics.GeoBoundsAggregation;
import com.liferay.portal.search.aggregation.metrics.GeoCentroidAggregation;
import com.liferay.portal.search.aggregation.metrics.MaxAggregation;
import com.liferay.portal.search.aggregation.metrics.MinAggregation;
import com.liferay.portal.search.aggregation.metrics.PercentileRanksAggregation;
import com.liferay.portal.search.aggregation.metrics.PercentilesAggregation;
import com.liferay.portal.search.aggregation.metrics.PercentilesMethod;
import com.liferay.portal.search.aggregation.metrics.ScriptedMetricAggregation;
import com.liferay.portal.search.aggregation.metrics.StatsAggregation;
import com.liferay.portal.search.aggregation.metrics.SumAggregation;
import com.liferay.portal.search.aggregation.metrics.ValueCountAggregation;
import com.liferay.portal.search.aggregation.metrics.WeightedAvgAggregation;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentileRanksAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStatsAggregationBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(
	immediate = true, property = "search.engine.impl=Elasticsearch",
	service = {AggregationTranslator.class, AggregationVisitor.class}
)
public class ElasticsearchAggregationVisitor
	implements AggregationTranslator<AggregationBuilder>,
			   AggregationVisitor<AggregationBuilder> {

	@Override
	public AggregationBuilder translate(Aggregation aggregation) {
		return aggregation.accept(this);
	}

	@Override
	public AggregationBuilder visit(AvgAggregation avgAggregation) {
		return _baseMetricsAggregationTranslator.translate(
			baseMetricsAggregation ->
				AggregationBuilders.avg(
					baseMetricsAggregation.getAggregationName()),
			avgAggregation, this);
	}

	@Override
	public AggregationBuilder visit(
		CardinalityAggregation cardinalityAggregation) {

		CardinalityAggregationBuilder cardinalityAggregationBuilder =
			(CardinalityAggregationBuilder)
				_baseMetricsAggregationTranslator.translate(
					baseMetricsAggregation ->
						AggregationBuilders.cardinality(
							baseMetricsAggregation.getAggregationName()),
					cardinalityAggregation, this);

		if (cardinalityAggregation.getPrecisionThreshold() != null) {
			cardinalityAggregationBuilder.precisionThreshold(
				cardinalityAggregation.getPrecisionThreshold());
		}

		return cardinalityAggregationBuilder;
	}

	@Override
	public AggregationBuilder visit(
		ExtendedStatsAggregation extendedStatsAggregation) {

		ExtendedStatsAggregationBuilder extendedStatsAggregationBuilder =
			(ExtendedStatsAggregationBuilder)
				_baseMetricsAggregationTranslator.translate(
					baseMetricsAggregation ->
						AggregationBuilders.extendedStats(
							baseMetricsAggregation.getAggregationName()),
					extendedStatsAggregation, this);

		if (extendedStatsAggregation.getSigma() != null) {
			extendedStatsAggregationBuilder.sigma(
				extendedStatsAggregation.getSigma());
		}

		return extendedStatsAggregationBuilder;
	}

	@Override
	public AggregationBuilder visit(GeoBoundsAggregation geoBoundsAggregation) {
		return _geoBoundsAggregationTranslator.translate(
			geoBoundsAggregation, this);
	}

	@Override
	public AggregationBuilder visit(
		GeoCentroidAggregation geoCentroidAggregation) {

		return _geoCentroidAggregationTranslator.translate(
			geoCentroidAggregation, this);
	}

	@Override
	public AggregationBuilder visit(MaxAggregation maxAggregation) {
		return _baseMetricsAggregationTranslator.translate(
			baseMetricsAggregation ->
				AggregationBuilders.max(
					baseMetricsAggregation.getAggregationName()),
			maxAggregation, this);
	}

	@Override
	public AggregationBuilder visit(MinAggregation minAggregation) {
		return _baseMetricsAggregationTranslator.translate(
			baseMetricsAggregation ->
				AggregationBuilders.min(
					baseMetricsAggregation.getAggregationName()),
			minAggregation, this);
	}

	@Override
	public AggregationBuilder visit(
		final PercentileRanksAggregation percentileRanksAggregation) {

		PercentileRanksAggregationBuilder percentileRanksAggregationBuilder =
			(PercentileRanksAggregationBuilder)
				_baseMetricsAggregationTranslator.translate(
					baseMetricsAggregation ->
						AggregationBuilders.percentileRanks(
							baseMetricsAggregation.getAggregationName(),
							percentileRanksAggregation.getValues()),
					percentileRanksAggregation, this);

		if (percentileRanksAggregation.getCompression() != null) {
			percentileRanksAggregationBuilder.compression(
				percentileRanksAggregation.getCompression());
		}

		if (percentileRanksAggregation.getHdrSignificantValueDigits() != null) {
			percentileRanksAggregationBuilder.numberOfSignificantValueDigits(
				percentileRanksAggregation.getHdrSignificantValueDigits());
		}

		if (percentileRanksAggregation.getKeyed() != null) {
			percentileRanksAggregationBuilder.keyed(
				percentileRanksAggregation.getKeyed());
		}

		if (percentileRanksAggregation.getPercentilesMethod() != null) {
			PercentilesMethod percentilesMethod =
				percentileRanksAggregation.getPercentilesMethod();

			percentileRanksAggregationBuilder.method(
				org.elasticsearch.search.aggregations.metrics.percentiles.
					PercentilesMethod.valueOf(percentilesMethod.name()));
		}

		return percentileRanksAggregationBuilder;
	}

	@Override
	public AggregationBuilder visit(
		PercentilesAggregation percentilesAggregation) {

		PercentilesAggregationBuilder percentilesAggregationBuilder =
			(PercentilesAggregationBuilder)
				_baseMetricsAggregationTranslator.translate(
					baseMetricsAggregation ->
						AggregationBuilders.percentiles(
							baseMetricsAggregation.getAggregationName()),
					percentilesAggregation, this);

		if (percentilesAggregation.getCompression() != null) {
			percentilesAggregationBuilder.compression(
				percentilesAggregation.getCompression());
		}

		if (percentilesAggregation.getHdrSignificantValueDigits() != null) {
			percentilesAggregationBuilder.numberOfSignificantValueDigits(
				percentilesAggregation.getHdrSignificantValueDigits());
		}

		if (percentilesAggregation.getKeyed() != null) {
			percentilesAggregationBuilder.keyed(
				percentilesAggregation.getKeyed());
		}

		if (percentilesAggregation.getPercentilesMethod() != null) {
			PercentilesMethod percentilesMethod =
				percentilesAggregation.getPercentilesMethod();

			percentilesAggregationBuilder.method(
				org.elasticsearch.search.aggregations.metrics.percentiles.
					PercentilesMethod.valueOf(percentilesMethod.name()));
		}

		return percentilesAggregationBuilder;
	}

	@Override
	public AggregationBuilder visit(
		ScriptedMetricAggregation scriptedMetricAggregation) {

		return _scriptedMetricAggregationTranslator.translate(
			scriptedMetricAggregation, this);
	}

	@Override
	public AggregationBuilder visit(StatsAggregation statsAggregation) {
		return _baseMetricsAggregationTranslator.translate(
			baseMetricsAggregation ->
				AggregationBuilders.stats(
					baseMetricsAggregation.getAggregationName()),
			statsAggregation, this);
	}

	@Override
	public AggregationBuilder visit(SumAggregation sumAggregation) {
		return _baseMetricsAggregationTranslator.translate(
			baseMetricsAggregation ->
				AggregationBuilders.sum(
					baseMetricsAggregation.getAggregationName()),
			sumAggregation, this);
	}

	@Override
	public AggregationBuilder visit(TermsAggregation termsAggregation) {
		return _baseBucketAggregationTranslator.translate(
			baseMetricsAggregation ->
				AggregationBuilders.terms(
					baseMetricsAggregation.getAggregationName()),
			termsAggregation, this);
	}

	@Override
	public AggregationBuilder visit(
		ValueCountAggregation valueCountAggregation) {

		return _baseMetricsAggregationTranslator.translate(
			baseMetricsAggregation ->
				AggregationBuilders.count(
					baseMetricsAggregation.getAggregationName()),
			valueCountAggregation, this);
	}

	@Override
	public AggregationBuilder visit(
		WeightedAvgAggregation weightedAvgAggregation) {

		return _weightedAvgAggregationTranslator.translate(
			weightedAvgAggregation, this);
	}

	private final BaseBucketAggregationTranslator
		_baseBucketAggregationTranslator =
			new BaseBucketAggregationTranslator();
	private final BaseMetricsAggregationTranslator
		_baseMetricsAggregationTranslator =
			new BaseMetricsAggregationTranslator();

	@Reference
	private GeoBoundsAggregationTranslator _geoBoundsAggregationTranslator;

	@Reference
	private GeoCentroidAggregationTranslator _geoCentroidAggregationTranslator;

	@Reference
	private ScriptedMetricAggregationTranslator
		_scriptedMetricAggregationTranslator;

	@Reference
	private WeightedAvgAggregationTranslator _weightedAvgAggregationTranslator;

}