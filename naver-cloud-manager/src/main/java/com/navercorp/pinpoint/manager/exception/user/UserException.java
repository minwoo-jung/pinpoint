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

package com.navercorp.pinpoint.manager.exception.user;

/**
 * @author HyunGil Jeong
 */
public class UserException extends RuntimeException {

    private final String userId;

    public UserException(String userId, String message) {
        super(message);
        this.userId = userId;
    }

    public UserException(String userId, String message, Throwable cause) {
        super(message, cause);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
