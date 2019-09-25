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

package com.liferay.portal.search.test.util;

import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author André de Oliveira
 */
public class FieldValuesAssert {

	public static void assertFieldValues(
		Map<String, String> expected, Document document, String message) {

		AssertUtils.assertEquals(
			message, expected, _getFieldValues(document, null));
	}

	public static void assertFieldValues(
		Map<String, String> expected, String prefix, Document document,
		String message) {

		AssertUtils.assertEquals(
			message, expected,
			_getFieldValues(document, name -> name.startsWith(prefix)));
	}

	private static Map<String, String> _getFieldValues(
		Document document, Predicate<String> predicate) {

		Map<String, Field> fieldsMap = document.getFields();

		Set<Map.Entry<String, Field>> entrySet = fieldsMap.entrySet();

		Stream<Map.Entry<String, Field>> stream = entrySet.stream();

		if (predicate != null) {
			stream = stream.filter(
				entry -> {
					if (Objects.equals(entry.getKey(), "ddmFields")) {
						Field fieldArray = entry.getValue();

						for (Field nestedField : fieldArray.getFields()) {
							for (Field field : nestedField.getFields()) {
								return Objects.equals(
									field.getName(), "ddmFieldName") &&
									   predicate.test(field.getValue());
							}
						}
					}

					return false;
				});
		}

		Map<String, String> fieldValues = new HashMap<>();

		stream.map(
			Map.Entry::getValue
		).map(
			Field::getFields
		).forEach(
			nestedFields -> {
				for (Field nestedField : nestedFields) {
					String name = null;
					String value = null;

					for (Field field : nestedField.getFields()) {
						if (StringUtil.contains(
								field.getName(), "ddmFieldName")) {

							name = field.getValue();
						}
					}

					for (Field field : nestedField.getFields()) {
						if (!StringUtil.contains(
								field.getName(), "ddmFieldName")) {

							String[] values = field.getValues();

							if (values == null) {
								continue;
							}

							if (values.length == 1) {
								value = values[0];
							}
							else {
								value = String.valueOf(Arrays.asList(values));
							}

							fieldValues.put(
								name +
									StringUtil.extractLast(
										field.getName(), "ddmFieldValue"),
								value);
						}
					}
				}
			}
		);

		return fieldValues;
	}

}