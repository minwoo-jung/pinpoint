/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.logging.PLogger_제거예정;
import com.navercorp.pinpoint.bootstrap.logging.SLF4jLoggerFactory;

/**
 * 
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public class PostProcessorInterceptor extends AbstractSpringBeanCreationInterceptor implements AroundInterceptor2 {
    private final PLogger_제거예정 logger = SLF4jLoggerFactory.getLogger(getClass());
    
    public PostProcessorInterceptor(Instrumentor instrumentor, TransformCallback transformer, TargetBeanFilter filter) {
        super(instrumentor, transformer, filter);
    }

    //
    @IgnoreMethod
    @Override
    public void before(Object target, Object arg0, Object arg1) {

    }

    @Override
    public void after(Object target, Object arg0, Object beanNameObject, Object result, Throwable throwable) {
        try {
            if (!(beanNameObject instanceof String)) {
                logger.warn("invalid type:{}", beanNameObject);
                return;
            }
            final String beanName = (String) beanNameObject;
            processBean(beanName, result);
        } catch (Throwable t) {
            logger.warn("Unexpected exception", t);
        }
    }
}
