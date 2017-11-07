<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
List<Map<String, Object>> linearData = new ArrayList<>();

Map<String, Object> linearDataCol1 = new HashMap<>();

int[] linearDataCol1Data = {100, 20, 30};

linearDataCol1.put("data", linearDataCol1Data);

linearDataCol1.put("id", "data1");

linearData.add(linearDataCol1);

Map<String, Object> linearDataCol2 = new HashMap<>();

int[] linearDataCol2Data = {20, 70, 100};

linearDataCol2.put("data", linearDataCol2Data);

linearDataCol2.put("id", "data2");

linearData.add(linearDataCol2);
%>

<div class="container-fluid">
	<div class="row">
		<div class="col">
			<chart:line columns="<%= linearData %>" id="line" />
		</div>

	</div>
</div>
