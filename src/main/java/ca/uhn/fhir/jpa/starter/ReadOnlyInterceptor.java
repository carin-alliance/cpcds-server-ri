package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;

import java.util.logging.Logger;

public class ReadOnlyInterceptor extends InterceptorAdapter {

	private static final Logger logger = ServerLogger.getLogger();
	private SecurityProperties securityProperties;

	public ReadOnlyInterceptor(SecurityProperties securityProperties) {
		super();
		this.securityProperties = securityProperties;
	}

	@Override
	public void incomingRequestPreHandled(RestOperationTypeEnum theOperation, RequestDetails theProcessedRequest) {

		if (!securityProperties.getReadOnly()) return;

		String authHeader = theProcessedRequest.getHeader("Authorization");
		String adminHeader = "Bearer " + securityProperties.getAdminToken();
		if (adminHeader.equals(authHeader)) return;
		else if (theOperation != RestOperationTypeEnum.HISTORY_INSTANCE
				&& theOperation != RestOperationTypeEnum.HISTORY_SYSTEM
				&& theOperation != RestOperationTypeEnum.HISTORY_TYPE
				&& theOperation != RestOperationTypeEnum.METADATA
				&& theOperation != RestOperationTypeEnum.READ
				&& theOperation != RestOperationTypeEnum.SEARCH_SYSTEM
				&& theOperation != RestOperationTypeEnum.SEARCH_TYPE
				&& theOperation != RestOperationTypeEnum.TRANSACTION
				&& theOperation != RestOperationTypeEnum.VALIDATE
				&& theOperation != RestOperationTypeEnum.VREAD) {
			logger.severe("ReadOnlyInterceptor::MethodNotAllowedException:" + theOperation.toString());
			throw new MethodNotAllowedException(theOperation.toString());
		}
	}
}
