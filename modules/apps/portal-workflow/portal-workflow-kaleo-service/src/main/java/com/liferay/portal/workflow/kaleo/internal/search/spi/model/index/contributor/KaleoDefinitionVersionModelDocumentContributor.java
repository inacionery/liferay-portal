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

package com.liferay.portal.workflow.kaleo.internal.search.spi.model.index.contributor;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;

import org.osgi.service.component.annotations.Component;

/**
 * @author Inácio Nery
 */
@Component(
	immediate = true,
	property = "indexer.class.name=com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion",
	service = ModelDocumentContributor.class
)
public class KaleoDefinitionVersionModelDocumentContributor
	implements ModelDocumentContributor<KaleoDefinitionVersion> {

	@Override
	public void contribute(
		Document document, KaleoDefinitionVersion kaleoDefinitionVersion) {

		document.addKeyword(
			"description", kaleoDefinitionVersion.getDescription());
		document.addNumberSortable(
			"kaleoDefinitionVersionId",
			kaleoDefinitionVersion.getKaleoDefinitionVersionId());
		document.addKeywordSortable("name", kaleoDefinitionVersion.getName());
		document.addKeyword(
			"startKaleoNodeId", kaleoDefinitionVersion.getStartKaleoNodeId());
		document.addLocalizedKeyword(
			"title",
			LocalizationUtil.populateLocalizationMap(
				kaleoDefinitionVersion.getTitleMap(),
				kaleoDefinitionVersion.getDefaultLanguageId(),
				kaleoDefinitionVersion.getGroupId()),
			true, true);
		document.addKeywordSortable(
			"version", kaleoDefinitionVersion.getVersion());
	}

}