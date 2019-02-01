package ${configYAML.apiPackagePath}.internal.servlet;

import ${configYAML.apiPackagePath}.query.Query;
import ${configYAML.apiPackagePath}.mutation.Mutation;
import com.liferay.portal.events.EventsProcessorUtil;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.util.PropsValues;
import com.liferay.portal.vulcan.context.AcceptLanguage;
import com.liferay.portal.vulcan.context.Pagination;
import com.liferay.portal.vulcan.dto.Page;

import graphql.Scalars;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.GraphQLAnnotations;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldDefinition.Builder;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import graphql.servlet.AbstractGraphQLHttpServlet;
import graphql.servlet.GraphQLInvocationInputFactory;
import graphql.servlet.GraphQLObjectMapper;
import graphql.servlet.GraphQLQueryInvoker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.Charset;

import java.util.Collections;

import javax.annotation.Generated;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author ${configYAML.author}
 * @generated
 */
@Component(
	immediate = true,
	property = {
		"osgi.http.whiteboard.context.path=${configYAML.application.baseURI}-graphql/",
		"osgi.http.whiteboard.servlet.name=${configYAML.apiPackagePath}.internal.servlet.GraphQLServlet",
		"osgi.http.whiteboard.servlet.pattern=${configYAML.application.baseURI}-graphql/*"
	},
	service = Servlet.class
)
public class GraphQLServlet extends AbstractGraphQLHttpServlet {

	protected GraphQLSchema createGraphQLQuerySchema() {

		return GraphQLSchema.newSchema().query(
			GraphQLAnnotations.object(Query.class)
		).mutation(
			GraphQLAnnotations.object(Mutation.class)
		).build();
	}

	@Override
	protected GraphQLObjectMapper getGraphQLObjectMapper() {
		GraphQLObjectMapper.Builder builder = GraphQLObjectMapper.newBuilder();

		return builder.build();
	}

	@Override
	protected GraphQLInvocationInputFactory getInvocationInputFactory() {
		GraphQLInvocationInputFactory.Builder builder =
			GraphQLInvocationInputFactory.newBuilder(
				createGraphQLQuerySchema());

		return builder.build();
	}

	@Override
	protected GraphQLQueryInvoker getQueryInvoker() {
		GraphQLQueryInvoker.Builder builder = GraphQLQueryInvoker.newBuilder();

		return builder.build();
	}

	private static final long serialVersionUID = 1L;
}