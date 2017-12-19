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

import com.nhncorp.owfs.Owfs;
import com.nhncorp.owfs.OwfsFile;
import com.nhncorp.owfs.OwfsOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jaehong.kim
 */
@Controller
public class OwfsController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/owfs/createOwner")
    @ResponseBody
    public String createOwner() {
        Owfs owfs = null;
        OwfsOwner owner = null;
        try {
            owfs = Owfs.init("10.105.64.187", "pinpoint_testweb");
            owner = new OwfsOwner(owfs, "owner_name");
            if (!owner.exists()) {
                owner.create();
            }
            owner.close();
            owfs.close();
        } catch (Exception e) {
            return e.getMessage();
        }

        return "OK";
    }

    @RequestMapping(value = "/owfs/writeFile")
    @ResponseBody
    public String writeFile() {
        Owfs owfs = null;
        OwfsOwner owner = null;
        try {
            owfs = Owfs.init("10.105.64.187", "pinpoint_testweb");
            owner = new OwfsOwner(owfs, "owner_name");
            owner.open();

            OwfsFile dir = new OwfsFile(owner, "/dir");
            if(!dir.exists()) {
                dir.mkdir();
            }

            OwfsFile file = new OwfsFile(owner, "/dir/greeting.txt");
            if(file.exists()) {
                file.delete();
            }
            file.open(true);
            byte[] buffer = "Hello World".getBytes();
            file.write(buffer, 0, buffer.length);
            file.close();
            owner.close();
            owfs.close();
        } catch (Exception e) {
            return e.getMessage();
        }

        return "OK";
    }
}