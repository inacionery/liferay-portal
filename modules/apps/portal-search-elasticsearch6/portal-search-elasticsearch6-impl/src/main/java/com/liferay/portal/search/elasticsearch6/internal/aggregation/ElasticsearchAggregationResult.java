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

import com.liferay.portal.search.aggregation.AggregationResults;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.aggregation.metrics.AvgAggregationResult;
import com.liferay.portal.search.aggregation.metrics.CardinalityAggregationResult;
import com.liferay.portal.search.aggregation.metrics.ExtendedStatsAggregationResult;
import com.liferay.portal.search.aggregation.metrics.GeoBoundsAggregationResult;
import com.liferay.portal.search.aggregation.metrics.GeoCentroidAggregationResult;
import com.liferay.portal.search.aggregation.metrics.GeoLocationPoint;
import com.liferay.portal.search.aggregation.metrics.MaxAggregationResult;
import com.liferay.portal.search.aggregation.metrics.MinAggregationResult;
import com.liferay.portal.search.aggregation.metrics.Percentile;
import com.liferay.portal.search.aggregation.metrics.PercentileRanksAggregationResult;
import com.liferay.portal.search.aggregation.metrics.PercentilesAggregationResult;
import com.liferay.portal.search.aggregation.metrics.ScriptedAggregationResult;
import com.liferay.portal.search.aggregation.metrics.StatsAggregationResult;
import com.liferay.portal.search.aggregation.metrics.SumAggregationResult;
import com.liferay.portal.search.aggregation.metrics.TopHitsAggregationResult;
import com.liferay.portal.search.aggregation.metrics.ValueCountAggregationResult;
import com.liferay.portal.search.aggregation.metrics.WeightedAvgAggregationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBounds;
import org.elasticsearch.search.aggregations.metrics.geocentroid.GeoCentroid;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentileRanks;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.scripted.ScriptedMetric;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStats;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.aggregations.metrics.weighted_avg.WeightedAvg;

/**
 * @author Inácio Nery
 */
public class ElasticsearchAggregationResult implements AggregationResults {

	public ElasticsearchAggregationResult(Aggregations aggregations) {
		_aggregations = aggregations;
	}

	@Override
	public AvgAggregationResult getAvgAggregationResult(
		String aggregationName) {

		Avg avg = _aggregations.get(aggregationName);

		if (avg == null) {
			return new AvgAggregationResult(0);
		}

		return new AvgAggregationResult(avg.getValue());
	}

	@Override
	public CardinalityAggregationResult getCardinalityAggregationResult(
		String aggregationName) {

		Cardinality cardinality = _aggregations.get(aggregationName);

		if (cardinality == null) {
			return new CardinalityAggregationResult(0);
		}

		return new CardinalityAggregationResult(cardinality.getValue());
	}

	@Override
	public ExtendedStatsAggregationResult getExtendedStatsAggregationResult(
		String aggregationName) {

		ExtendedStats extendedStats = _aggregations.get(aggregationName);

		if (extendedStats == null) {
			return new ExtendedStatsAggregationResult(0, 0, 0, 0, 0, 0, 0, 0);
		}

		return new ExtendedStatsAggregationResult(
			extendedStats.getAvg(), extendedStats.getCount(),
			extendedStats.getMax(), extendedStats.getMin(),
			extendedStats.getStdDeviation(), extendedStats.getSum(),
			extendedStats.getSumOfSquares(), extendedStats.getVariance());
	}

	@Override
	public GeoBoundsAggregationResult getGeoBoundsAggregationResult(
		String aggregationName) {

		GeoBounds geoBounds = _aggregations.get(aggregationName);

		if (geoBounds == null) {
			return new GeoBoundsAggregationResult(null, null);
		}

		GeoPoint bottomRight = geoBounds.bottomRight();

		GeoPoint topLeft = geoBounds.topLeft();

		return new GeoBoundsAggregationResult(
			new GeoLocationPoint(bottomRight.getLat(), bottomRight.getLon()),
			new GeoLocationPoint(topLeft.getLat(), topLeft.getLon()));
	}

	@Override
	public GeoCentroidAggregationResult getGeoCentroidAggregationResult(
		String aggregationName) {

		GeoCentroid geoCentroid = _aggregations.get(aggregationName);

		if (geoCentroid == null) {
			return new GeoCentroidAggregationResult(null, 0);
		}

		GeoPoint centroid = geoCentroid.centroid();

		return new GeoCentroidAggregationResult(
			new GeoLocationPoint(centroid.getLat(), centroid.getLon()),
			geoCentroid.count());
	}

	@Override
	public MaxAggregationResult getMaxAggregationResult(
		String aggregationName) {

		Max max = _aggregations.get(aggregationName);

		if (max == null) {
			return new MaxAggregationResult(0);
		}

		return new MaxAggregationResult(max.getValue());
	}

	@Override
	public MinAggregationResult getMinAggregationResult(
		String aggregationName) {

		Min min = _aggregations.get(aggregationName);

		if (min == null) {
			return new MinAggregationResult(0);
		}

		return new MinAggregationResult(min.getValue());
	}

	@Override
	public PercentileRanksAggregationResult getPercentileRanksAggregationResult(
		String aggregationName) {

		PercentileRanks percentileRanks = _aggregations.get(aggregationName);

		if (percentileRanks == null) {
			return new PercentileRanksAggregationResult(
				Collections.emptyList());
		}

		List<Percentile> percentiles = new ArrayList<>();

		percentileRanks.forEach(
			percentile -> percentiles.add(
				new Percentile(
					percentile.getPercent(), percentile.getValue())));

		return new PercentileRanksAggregationResult(percentiles);
	}

	@Override
	public PercentilesAggregationResult getPercentilesAggregationResult(
		String aggregationName) {

		Percentiles percentiles = _aggregations.get(aggregationName);

		if (percentiles == null) {
			return new PercentilesAggregationResult(Collections.emptyList());
		}

		List<Percentile> percentileList = new ArrayList<>();

		percentiles.forEach(
			percentile -> percentileList.add(
				new Percentile(
					percentile.getPercent(), percentile.getValue())));

		return new PercentilesAggregationResult(percentileList);
	}

	public ScriptedAggregationResult getScriptedAggregationResult(
		String aggregationName) {

		ScriptedMetric scriptedMetric = _aggregations.get(aggregationName);

		return new ScriptedAggregationResult(scriptedMetric.aggregation());
	}

	public StatsAggregationResult getStatsAggregationResult(
		String aggregationName) {

		Stats stats = _aggregations.get(aggregationName);

		return new StatsAggregationResult(
			stats.getAvg(), stats.getCount(), stats.getMax(), stats.getMin(),
			stats.getCount());
	}

	public SumAggregationResult getSumAggregationResult(
		String aggregationName) {

		Sum sum = _aggregations.get(aggregationName);

		if (sum == null) {
			return new SumAggregationResult(0);
		}

		return new SumAggregationResult(sum.getValue());
	}

	@Override
	public TermsAggregationResult getTermsAggregationResult(
		String aggregationName) {

		Terms terms = _aggregations.get(aggregationName);

		return new TermsAggregationResult(getBuckets(terms));
	}

	@Override
	public TopHitsAggregationResult getTopHitsAggregationResult(
		String aggregationName) {

		TopHits topHits = _aggregations.get(aggregationName);

		return _topHitsAggregationResultTranslator.translate(topHits);
	}

	@Override
	public ValueCountAggregationResult getValueCountAggregationResult(
		String aggregationName) {

		ValueCount valueCount = _aggregations.get(aggregationName);

		if (valueCount == null) {
			return new ValueCountAggregationResult(0);
		}

		return new ValueCountAggregationResult(valueCount.getValue());
	}

	@Override
	public WeightedAvgAggregationResult getWeightedAvgAggregationResult(
		String aggregationName) {

		WeightedAvg weightedAvg = _aggregations.get(aggregationName);

		if (weightedAvg == null) {
			return new WeightedAvgAggregationResult(0);
		}

		return new WeightedAvgAggregationResult(weightedAvg.getValue());
	}

	protected List<Bucket> getBuckets(Terms terms) {
		return Optional.ofNullable(
			terms
		).map(
			Terms::getBuckets
		).map(
			buckets -> buckets.stream()
		).orElse(
			Stream.empty()
		).map(
			bucket -> new Bucket(
				new ElasticsearchAggregationResult(bucket.getAggregations()),
				bucket.getDocCount(), bucket.getKeyAsString())
		).collect(
			Collectors.toList()
		);
	}

	private final Aggregations _aggregations;
	private TopHitsAggregationResultTranslator
		_topHitsAggregationResultTranslator =
			new TopHitsAggregationResultTranslator();

}