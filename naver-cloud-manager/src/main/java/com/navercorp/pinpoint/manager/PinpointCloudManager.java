/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.manager;

import static io.undertow.predicate.Predicates.not;
import static io.undertow.predicate.Predicates.path;

import io.undertow.predicate.Predicate;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.resource.*;
import io.undertow.servlet.api.DeploymentInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author HyunGil Jeong
 */
@SpringBootApplication
@ImportResource("classpath:applicationContext-manager.xml")
public class PinpointCloudManager {

    public static void main(String[] args) {
        SpringApplication.run(PinpointCloudManager.class, args);
    }

    @Bean
    public ServletRegistrationBean dispatchServletRegistration(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet, "*.pinpoint");
        return registration;
    }


    //static resource를 다룰 일이 생기면 사용하면 됨.
    //static resource는 Defaultservlet 자체를 통하지 않고 handler 레벨에서 반환을 해주는게 가장 좋을듯함.
    //그래야 인증 로직 자체를 거치지 않게 된다.
    //web-inf와 meta-inf 경로를 다루는 로직은 아직 미완성이고, 현재 resources 폴더에 static 파일이 있는것도 개선이 필요함.
    @Bean
    public UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
        UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();

        UndertowDeploymentInfoCustomizer customizer = new UndertowDeploymentInfoCustomizer() {
            @Override
            public void customize(DeploymentInfo deploymentInfo) {
                deploymentInfo.addInitialHandlerChainWrapper(new HandlerWrapper() {
                    @Override
                    public HttpHandler wrap(HttpHandler baseHandler) {
                        final ResourceManager resourceManager = new ClassPathResourceManager(this.getClass().getClassLoader(), "webapp/static/");
                        final ResourceHandler resourceHandler = new ResourceHandler(resourceManager);
                        resourceHandler.setDirectoryListingEnabled(false).setAllowed((not(path("META-INF"))));
                        Predicate predicate = new Predicate() {
                            @Override
                            public boolean resolve(HttpServerExchange value) {
                                try {
                                    Resource resource = resourceManager.getResource(value.getRelativePath());
                                    if (resource == null) {
                                        return false;
                                    }
                                    return true;
                                } catch (IOException ex) {
                                    return false;
                                }
                            }
                        };

                        return new PredicateHandler(predicate, resourceHandler, baseHandler);
                    }
                });
            }
        };

        factory.addDeploymentInfoCustomizers(customizer);

        return factory;
    }

}
