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

package com.liferay.portal.vulcan.pagination;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

/**
 * @author Alejandro Hernández
 * @author Zoltán Takács
 */
@GraphQLName("Pagination")
public class Pagination {

	public static Pagination of(int itemsPerPage, int pageNumber) {
		return new Pagination(itemsPerPage, pageNumber);
	}

	public int getEndPosition() {
		return pageNumber * itemsPerPage;
	}

	public int getItemsPerPage() {
		return itemsPerPage;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public int getStartPosition() {
		return (pageNumber - 1) * itemsPerPage;
	}

	@GraphQLField
	protected final int itemsPerPage;

	@GraphQLField
	protected final int pageNumber;

	private Pagination(int itemsPerPage, int pageNumber) {
		this.itemsPerPage = itemsPerPage;
		this.pageNumber = pageNumber;
	}

}