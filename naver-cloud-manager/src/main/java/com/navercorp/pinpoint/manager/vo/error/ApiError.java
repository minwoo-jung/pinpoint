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

package com.navercorp.pinpoint.manager.vo.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ApiError {
    // TODO implement proper common error response
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private final LocalDateTime timestamp;
    private final HttpStatus httpStatus;
    private final String message;
    private final String debugMessage;
    private final List<ApiSubError> subErrors = new ArrayList<>();

    public ApiError(Throwable t) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, t);
    }

    public ApiError(HttpStatus httpStatus, Throwable t) {
        this(httpStatus, t.getMessage());
    }

    public ApiError(HttpStatus httpStatus, String message) {
        this.timestamp = LocalDateTime.now();
        this.httpStatus = httpStatus;
        this.message = message;
        this.debugMessage = null;
    }

    public ApiError(HttpStatus httpStatus, String message, Throwable t) {
        this.timestamp = LocalDateTime.now();
        this.httpStatus = httpStatus;
        this.message = message;
        this.debugMessage = t.getMessage();
    }

    public final void addSubError(ApiSubError subError) {
        if (subError == null) {
            return;
        }
        subErrors.add(subError);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public List<ApiSubError> getSubErrors() {
        return subErrors;
    }
}
