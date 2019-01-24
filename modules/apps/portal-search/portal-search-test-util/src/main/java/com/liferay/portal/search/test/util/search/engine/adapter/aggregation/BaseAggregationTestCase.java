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

package com.liferay.portal.search.test.util.search.engine.adapter.aggregation;

import com.liferay.portal.kernel.search.generic.MatchAllQuery;
import com.liferay.portal.search.aggregation.Aggregation;
import com.liferay.portal.search.aggregation.AggregationResults;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;

import org.junit.Assert;

/**
 * @author Rafael Praxedes
 */
public abstract class BaseAggregationTestCase extends BaseIndexingTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();

		_searchEngineAdapter = getSearchEngineAdapter();
	}

	protected abstract SearchEngineAdapter getSearchEngineAdapter();

	protected AggregationResults search(Aggregation... aggregations) {
		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(
				new SearchSearchRequest() {
					{
						setIndexNames("_all");
						setQuery(new MatchAllQuery());

						for (Aggregation aggregation : aggregations) {
							addAggregation(aggregation);
						}
					}
				});

		AggregationResults aggregationResults =
			searchSearchResponse.getAggregationResults();

		Assert.assertNotNull(aggregationResults);

		return aggregationResults;
	}

	private SearchEngineAdapter _searchEngineAdapter;

}