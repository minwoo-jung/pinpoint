/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.manager.controller;

import com.navercorp.pinpoint.manager.exception.database.DatabaseManagementException;
import com.navercorp.pinpoint.manager.exception.database.DuplicateDatabaseException;
import com.navercorp.pinpoint.manager.exception.database.UnknownDatabaseException;
import com.navercorp.pinpoint.manager.exception.hbase.DuplicateHbaseException;
import com.navercorp.pinpoint.manager.exception.hbase.HbaseManagementException;
import com.navercorp.pinpoint.manager.exception.hbase.UnknownHbaseException;
import com.navercorp.pinpoint.manager.exception.repository.DuplicateRepositoryException;
import com.navercorp.pinpoint.manager.exception.repository.InvalidRepositoryStateException;
import com.navercorp.pinpoint.manager.exception.repository.RepositoryException;
import com.navercorp.pinpoint.manager.exception.repository.UnknownRepositoryException;
import com.navercorp.pinpoint.manager.vo.error.ApiError;
import com.navercorp.pinpoint.manager.vo.error.ApiSubError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author HyunGil Jeong
 */
@ControllerAdvice
public class ErrorResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = "Parameter validation failed for " + ex.getParameter();
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, message, ex);
        BindingResult bindingResult = ex.getBindingResult();
        if (bindingResult != null) {
            for (ObjectError objectError : bindingResult.getAllErrors()) {
                apiError.addSubError(new MethodArgumentNotValidSubError(objectError));
            }
        }
        return buildErrorResponse(apiError);
    }

    private static class MethodArgumentNotValidSubError implements ApiSubError {

        private final String message;

        private MethodArgumentNotValidSubError(ObjectError error) {
            this.message = error.toString();
        }

        public String getMessage() {
            return message;
        }
    }

    @ExceptionHandler(value = {UnknownRepositoryException.class, UnknownDatabaseException.class, UnknownHbaseException.class})
    public ResponseEntity<Object> handleUnknownExceptions(UnknownRepositoryException e) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, e);
        return buildErrorResponse(apiError);
    }

    @ExceptionHandler(value = {DuplicateRepositoryException.class, DuplicateDatabaseException.class, DuplicateHbaseException.class})
    public ResponseEntity<Object> handleDuplicateExceptions(Exception e) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT, e);
        return buildErrorResponse(apiError);
    }

    @ExceptionHandler(value = InvalidRepositoryStateException.class)
    public ResponseEntity<Object> handleInvalidRepositoryStateException(InvalidRepositoryStateException e, WebRequest webRequest) {
        final String path = webRequest.getContextPath();
        logger.error("{} Invalid repository state error for {}", path, e.getOrganizationName(), e);
        ApiError apiError = new ApiError(HttpStatus.CONFLICT, "Invalid repository state", e);
        return buildErrorResponse(apiError);
    }

    @ExceptionHandler(value = DatabaseManagementException.class)
    public ResponseEntity<Object> handleDatabaseManagementException(DatabaseManagementException e, WebRequest webRequest) {
        final String path = webRequest.getContextPath();
        logger.error("{} Database management error for {}", path, e.getDatabaseName(), e);
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected database error", e);
        return buildErrorResponse(apiError);
    }

    @ExceptionHandler(value = HbaseManagementException.class)
    public ResponseEntity<Object> handleHbaseManagementException(HbaseManagementException e, WebRequest webRequest) {
        final String path = webRequest.getContextPath();
        logger.error("{} Hbase management error for {}", path, e.getHbaseNamespace(), e);
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected hbase error", e);
        return buildErrorResponse(apiError);
    }

    @ExceptionHandler(value = RepositoryException.class)
    public ResponseEntity<Object> handleRepositoryException(RepositoryException e, WebRequest webRequest) {
        final String path = webRequest.getContextPath();
        logger.error("{} Unexpected repository error for {}", path, e.getOrganizationName(), e);
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected repository error", e);
        return buildErrorResponse(apiError);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleAll(Exception e, WebRequest webRequest) {
        final String path = webRequest.getContextPath();
        logger.error("{} Unexpected error", path, e);
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e);
        return buildErrorResponse(apiError);
    }

    private ResponseEntity<Object> buildErrorResponse(ApiError apiError) {
        return ResponseEntity.status(apiError.getHttpStatus()).body(apiError);
    }
}
