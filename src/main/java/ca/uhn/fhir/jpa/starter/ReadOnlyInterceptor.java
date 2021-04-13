package ca.uhn.fhir.jpa.starter;

import java.util.logging.Logger;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;

public class ReadOnlyInterceptor extends InterceptorAdapter {

    private static final Logger logger = ServerLogger.getLogger();

    @Override
    public void incomingRequestPreHandled(RestOperationTypeEnum theOperation,
            ActionRequestDetails theProcessedRequest) {
        String authHeader = theProcessedRequest.getRequestDetails().getHeader("Authorization");
        String adminToken = System.getenv("ADMIN_TOKEN");
        if (!authHeader.equals("Bearer " + adminToken)
                && theOperation != RestOperationTypeEnum.HISTORY_INSTANCE
                && theOperation != RestOperationTypeEnum.HISTORY_SYSTEM
                && theOperation != RestOperationTypeEnum.HISTORY_TYPE && theOperation != RestOperationTypeEnum.METADATA
                && theOperation != RestOperationTypeEnum.READ && theOperation != RestOperationTypeEnum.SEARCH_SYSTEM
                && theOperation != RestOperationTypeEnum.SEARCH_TYPE
                && theOperation != RestOperationTypeEnum.TRANSACTION && theOperation != RestOperationTypeEnum.VALIDATE
                && theOperation != RestOperationTypeEnum.VREAD) {
            logger.severe("ReadOnlyInterceptor::MethodNotAllowedException:" + theOperation.toString()); 
            throw new MethodNotAllowedException(theOperation.toString());
        }
    }
}