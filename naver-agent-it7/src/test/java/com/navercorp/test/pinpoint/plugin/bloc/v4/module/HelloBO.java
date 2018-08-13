/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.test.pinpoint.plugin.bloc.v4.module;

import com.nhncorp.lucy.bloc.annotation.Procedure;
import com.nhncorp.lucy.bloc.annotation.Resource;

/**
 * @author Jongho Moon
 *
 */
@Resource(name="hello")
public class HelloBO {
    @Procedure
    public String sayHello(String name) {
        return "hello " + name + "!";
    }
}
