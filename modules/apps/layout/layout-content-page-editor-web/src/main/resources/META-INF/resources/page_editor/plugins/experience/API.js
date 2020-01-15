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

import React from 'react';

import ExperienceService from '../../app/services/ExperienceService';

const {
	createExperience,
	removeExperience,
	selectExperience,
	updateExperience,
	updateExperiencePriority
} = ExperienceService;

export const APIContext = React.createContext({});

const API = config => ({
	createExperience: ({name, segmentsEntryId}) =>
		createExperience({
			body: {name, segmentsEntryId},
			config
		}),
	removeExperience: (
		segmentsExperienceId,
		fragmentEntryLinkIds = [],
		selectedExperienceId
	) =>
		removeExperience({
			body: {
				fragmentEntryLinkIds,
				segmentsExperienceId,
				selectedExperienceId
			},
			config
		}),
	selectExperience: ({segmentsExperienceId}) =>
		selectExperience({body: {segmentsExperienceId}, config}),
	updateExperience: ({active, name, segmentsEntryId, segmentsExperienceId}) =>
		updateExperience({
			body: {
				active,
				name,
				segmentsEntryId,
				segmentsExperienceId
			},
			config
		}),
	updateExperiencePriority: ({newPriority, segmentsExperienceId}) =>
		updateExperiencePriority({
			body: {
				newPriority,
				segmentsExperienceId
			},
			config
		})
});

export default API;
