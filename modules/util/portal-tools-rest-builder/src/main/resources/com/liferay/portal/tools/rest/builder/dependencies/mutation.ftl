package ${configYAML.apiPackagePath}.mutation;

<#compress>
	<#list openAPIYAML.components.schemas?keys as schemaName>
		import ${configYAML.apiPackagePath}.dto.${schemaName};
	</#list>
</#compress>

import com.liferay.oauth2.provider.scope.RequiresScope;
import com.liferay.portal.vulcan.context.AcceptLanguage;
import com.liferay.portal.vulcan.context.Pagination;
import com.liferay.portal.vulcan.dto.Page;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.GraphQLAnnotations;

import graphql.schema.DataFetchingEnvironment;

import java.util.List;

import javax.annotation.Generated;

/**
 * To access this query, run:
 *
 *     curl -u your@email.com:yourpassword -D - http://localhost:8080/o${configYAML.application.baseURI}-graphql
 *
 * @author ${configYAML.author}
 * @generated
 */
@Generated("")
public interface Mutation {

	<#list openAPIYAML.pathItems?keys as path>
		<#assign
			pathItem = openAPIYAML.pathItems[path]

			operationNames = ""
		/>

		<#if pathItem.post??>
			<#assign operationNames = "${operationNames},post" />
		</#if>

		<#list stringUtil.split(operationNames, ",") as operationName>
			<#if !operationName?trim?has_content>
				<#continue>
			</#if>

			<#if stringUtil.equals(operationName, "post")>
				<#assign
					annotationHTTPMethod = "@GraphQLField"

					operation = pathItem.post
				/>
			</#if>

			<#assign methodParameters = "" />

			<#if operation.parameters??>
				<#assign
					acceptLanguageParameter = false
					pageParameter = false
					perPageParameter = false
				/>

				<#list operation.parameters as parameter>
					<#if stringUtil.equals(parameter.name, "Accept-Language")>
						<#assign acceptLanguageParameter = true />
					<#elseif stringUtil.equals(parameter.name, "page")>
						<#assign pageParameter = true />
					<#elseif stringUtil.equals(parameter.name, "per_page")>
						<#assign perPageParameter = true />
					</#if>
				</#list>

				<#assign methodParameters>
					<@compress single_line=true>
						<#list operation.parameters as parameter>
							<#if acceptLanguageParameter && stringUtil.equals(parameter.name, "Accept-Language")>
								<#continue>
							</#if>

							<#if pageParameter && perPageParameter && (stringUtil.equals(parameter.name, "page") || stringUtil.equals(parameter.name, "per_page"))>
								<#continue>
							</#if>

							<#if parameter.schema.type??>
								@GraphQLName("${parameter.name}")

								<#if parameter.schema.format?? && stringUtil.equals(parameter.schema.format, "int64") && stringUtil.equals(parameter.schema.type, "integer")>
									Long
								<#else>
									${parameter.schema.type?cap_first}
								</#if>
							<#elseif parameter.schema.reference??>
								<#assign reference = "${parameter.schema.reference}" />

								${reference[(reference?last_index_of('/') + 1)..(reference?length - 1)]}
							</#if>

							<#assign parameterName = "" />

							<#list parameter.name?split("[^A-Za-z0-9]", "r") as s>
								<#if s?has_content>
									<#if parameterName?has_content>
										<#assign parameterName = "${parameterName}${s?cap_first}" />
									<#else>
										<#assign parameterName = "${s}" />
									</#if>
								</#if>
							</#list>

							${parameterName},
						</#list>
					</@compress>
				</#assign>

				<#if pageParameter && perPageParameter>
					<#assign methodParameters = "${methodParameters} @GraphQLName('Pagination') Pagination pagination," />
				</#if>

				<#assign methodParameters = "final DataFetchingEnvironment env, ${methodParameters}" />

				<#if methodParameters?has_content>
					<#assign methodParameters = "${methodParameters[0..(methodParameters?length - 2)]}" />
				</#if>
			</#if>

			<#assign
				methodReturnType = "Response"
				methodReturnValue = "Response"
			/>

			<#if operation.responses??>
				<#list operation.responses?values as response>
					<#if response.content??>
						<#list response.content?values as content>
							<#if content.schema??>
								<#assign schema = content.schema />

								<#if schema.type??>
									<#if stringUtil.equals(schema.type, "array")>
										<#assign reference = "${schema.items.reference}" />

										<#if reference?contains("/schemas/")>
											<#assign name = "${reference[(reference?last_index_of('/') + 1)..(reference?length - 1)]}" />

											<#assign
												methodReturnType = "List"
												methodReturnValue = "List<${name}>"
											/>
										</#if>
									</#if>
								</#if>

								<#if schema.reference??>
									<#assign reference = "${schema.reference}" />

									<#if reference?contains("/schemas/")>
										<#assign name = "${reference[(reference?last_index_of('/') + 1)..(reference?length - 1)]}" />

										<#assign
											methodReturnType = "${name}"
											methodReturnValue = "${name}"
										/>
									</#if>
								</#if>
							</#if>
						</#list>
					</#if>
				</#list>
			</#if>

			<#assign name>
				<@compress single_line=true>
					${operationName?lower_case}

					<#list path?replace("\\{.*?\\}", "", "rs")?split("[^A-Za-z0-9]", "r") as s>
						<#if s?has_content>
							${s?cap_first}
						</#if>
					</#list>
				</@compress>
			</#assign>

			<#assign name = "${name?replace(' ', '')}" />

			<#if stringUtil.equals(methodReturnType, "List") && !stringUtil.endsWith(name, "List")>
				<#assign name = "${name}List" />
			<#elseif stringUtil.endsWith(name, methodReturnType + "s")>
				<#assign name = "${name[0..(name?length - 2)]}" />
			</#if>

			<#assign template>
				${annotationHTTPMethod}
				@GraphQLInvokeDetached
				public ${methodReturnValue} ${name}(${methodParameters}) throws Exception;
			</#assign>

			<#list template?split("\n") as line>
				<#if line?trim?has_content>
${line?replace("^\t\t\t", "", "r")}
				</#if>
			</#list>
		</#list>
	</#list>

}