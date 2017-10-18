/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.test.pinpoint.testweb.controller;

import com.navercorp.test.pinpoint.testweb.domain.Member;
import com.navercorp.test.pinpoint.testweb.service.hystrix.HystrixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Controller
@RequestMapping("/hystrix")
public class HystrixController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("hystrixCommandExecuteService")
    private HystrixService<List<Member>> hystrixCommandExecuteService;

    @Autowired
    @Qualifier("hystrixCommandQueueService")
    private HystrixService<List<Member>> hystrixCommandQueueService;

    @Autowired
    @Qualifier("hystrixCommandObserveService")
    private HystrixService<Observable<List<Member>>> hystrixCommandObserveService;

    @Autowired
    @Qualifier("hystrixCommandToObservableService")
    private HystrixService<Observable<List<Member>>> hystrixCommandToObservableService;

    @Autowired
    @Qualifier("hystrixObservableCommandObserveService")
    private HystrixService<Observable<Member>> hystrixObservableCommandObserveService;

    @Autowired
    @Qualifier("hystrixObservableCommandToObservableService")
    private HystrixService<Observable<Member>> hystrixObservableCommandToObservableService;

    @RequestMapping("/command/execute")
    @ResponseBody
    public List<Member> commandExecute() {
        return hystrixCommandExecuteService.getMembers();
    }

    @RequestMapping("/command/execute/exception")
    @ResponseBody
    public List<Member> commandExecuteException() {
        return hystrixCommandExecuteService.getMembersException();
    }

    @RequestMapping("/command/execute/timeout")
    @ResponseBody
    public List<Member> commandExecuteTimeout() {
        return hystrixCommandExecuteService.getMembersTimeout();
    }

    @RequestMapping("/command/execute/shortCircuit")
    @ResponseBody
    public List<Member> commandExecuteShortCircuit() {
        return hystrixCommandExecuteService.getMembersShortCircuit();
    }


    @RequestMapping("/command/queue")
    @ResponseBody
    public List<Member> commandQueue() {
        return hystrixCommandQueueService.getMembers();
    }

    @RequestMapping("/command/queue/exception")
    @ResponseBody
    public List<Member> commandQueueException() {
        return hystrixCommandQueueService.getMembersException();
    }

    @RequestMapping("/command/queue/timeout")
    @ResponseBody
    public List<Member> commandQueueTimeout() {
        return hystrixCommandQueueService.getMembersTimeout();
    }

    @RequestMapping("/command/queue/shortCircuit")
    @ResponseBody
    public List<Member> commandQueueShortCircuit() {
        return hystrixCommandQueueService.getMembersShortCircuit();
    }


    @RequestMapping("/command/observe")
    @ResponseBody
    public List<Member> commandObserve() {
        return hystrixCommandObserveService.getMembers().toBlocking().single();
    }

    @RequestMapping("/command/observe/exception")
    @ResponseBody
    public List<Member> commandObserveException() {
        return hystrixCommandObserveService.getMembersException().toBlocking().single();
    }

    @RequestMapping("/command/observe/timeout")
    @ResponseBody
    public List<Member> commandObserveTimeout() {
        return hystrixCommandObserveService.getMembersTimeout().toBlocking().single();
    }

    @RequestMapping("/command/observe/shortCircuit")
    @ResponseBody
    public List<Member> commandObserveShortCircuit() {
        return hystrixCommandObserveService.getMembersShortCircuit().toBlocking().single();
    }


    @RequestMapping("/command/toObservable")
    @ResponseBody
    public List<Member> commandToObservable() {
        return hystrixCommandToObservableService.getMembers().toBlocking().single();
    }

    @RequestMapping("/command/toObservable/exception")
    @ResponseBody
    public List<Member> commandToObservableException() {
        return hystrixCommandToObservableService.getMembersException().toBlocking().single();
    }

    @RequestMapping("/command/toObservable/timeout")
    @ResponseBody
    public List<Member> commandToObservableTimeout() {
        return hystrixCommandToObservableService.getMembersTimeout().toBlocking().single();
    }

    @RequestMapping("/command/toObservable/shortCircuit")
    @ResponseBody
    public List<Member> commandToObservableShortCircuit() {
        return hystrixCommandToObservableService.getMembersShortCircuit().toBlocking().single();
    }


    @RequestMapping("/observableCommand/observe")
    @ResponseBody
    public List<Member> observableCommandObserve() {
        return hystrixObservableCommandObserveService.getMembers()
                .subscribeOn(Schedulers.io())
                .toList()
                .toBlocking()
                .single();
    }

    @RequestMapping("/observableCommand/observe/exception")
    @ResponseBody
    public List<Member> observableCommandObserveException() {
        return hystrixObservableCommandObserveService.getMembersException()
                .subscribeOn(Schedulers.io())
                .toList()
                .toBlocking()
                .single();
    }

    @RequestMapping("/observableCommand/observe/timeout")
    @ResponseBody
    public List<Member> observableCommandObserveTimeout() {
        return hystrixObservableCommandObserveService.getMembersTimeout()
                .subscribeOn(Schedulers.io())
                .toList()
                .toBlocking()
                .single();
    }

    @RequestMapping("/observableCommand/observe/shortCircuit")
    @ResponseBody
    public List<Member> observableCommandObserveShortCircuit() {
        return hystrixObservableCommandObserveService.getMembersShortCircuit()
                .subscribeOn(Schedulers.io())
                .toList()
                .toBlocking()
                .single();
    }


    @RequestMapping("/observableCommand/toObservable")
    @ResponseBody
    public List<Member> observableCommandToObservable() {
        return hystrixObservableCommandToObservableService.getMembers()
                .subscribeOn(Schedulers.io())
                .toList()
                .toBlocking()
                .single();
    }

    @RequestMapping("/observableCommand/toObservable/exception")
    @ResponseBody
    public List<Member> observableCommandToObservableException() {
        return hystrixObservableCommandToObservableService.getMembersException()
                .subscribeOn(Schedulers.io())
                .toList()
                .toBlocking()
                .single();
    }

    @RequestMapping("/observableCommand/toObservable/timeout")
    @ResponseBody
    public List<Member> observableCommandToObservableTimeout() {
        return hystrixObservableCommandToObservableService.getMembersTimeout()
                .subscribeOn(Schedulers.io())
                .toList()
                .toBlocking()
                .single();
    }

    @RequestMapping("/observableCommand/toObservable/shortCircuit")
    @ResponseBody
    public List<Member> observableCommandToObservableShortCircuit() {
        return hystrixObservableCommandToObservableService.getMembersShortCircuit()
                .subscribeOn(Schedulers.io())
                .toList()
                .toBlocking()
                .single();
    }
}
