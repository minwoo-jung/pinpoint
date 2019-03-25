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
package com.navercorp.pinpoint.web.security;

/**
 * @author minwoo.jung
 */
public final class LoginStaticString {

    public static final String LOGIN_URL =  "login.pinpoint";

    public static final String LOGIN_PROCESSING_URL = "j_spring_security_check.pinpoint";

    public static final String LOGOUT_SUCCESS_URL = "/login.pinpoint?logout";

}
