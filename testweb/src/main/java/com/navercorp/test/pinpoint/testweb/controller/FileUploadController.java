/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.test.pinpoint.testweb.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Copied directly from this URL.
 * http://www.journaldev.com/2573/spring-mvc-file-upload-example-tutorial-single-and-multiple-files
 */
@Controller
@RequestMapping("fileUpload")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    private static final String TEMP_FILE_LOCATION = getTempFileLocation();
    /**
     * Upload single file using Spring Controller
     */
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public @ResponseBody
    String uploadFileHandler(@RequestParam("name") String name, @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return "You failed to upload " + name + " because the file was empty.";
        }

        try {
            final byte[] bytes = file.getBytes();

            // Creating the directory to store file
            File dir = getDirectory(TEMP_FILE_LOCATION);

            // Create the file on server
            File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
            FileUtils.writeByteArrayToFile(serverFile, bytes);

            logger.info("Server File Location={}", serverFile.getAbsolutePath());

            return "You successfully uploaded file=" + name;
        } catch (Exception e) {
            return "You failed to upload " + name + " => " + e.getMessage();
        }

    }

    /**
     * Upload multiple file using Spring Controller
     */
    @RequestMapping(value = "/uploadMultipleFile", method = RequestMethod.POST)
    public @ResponseBody
    String uploadMultipleFileHandler(@RequestParam("name") String[] names, @RequestParam("file") MultipartFile[] files) {

        if (files.length != names.length) {
            return "Mandatory information missing";
        }

        final File saveDir = getDirectory(TEMP_FILE_LOCATION);
        final List<String> fileNames = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            final MultipartFile file = files[i];
            final String name = names[i];
            try {
                final byte[] bytes = file.getBytes();

                // Create the file on server
                File serverFile = new File(saveDir.getAbsolutePath() + File.separator + name);
                FileUtils.writeByteArrayToFile(serverFile, bytes);

                logger.info("Server File Location={}", serverFile.getAbsolutePath());

                fileNames.add(name);
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        }
        return formatFileList(fileNames);
    }

    private String formatFileList(List<String> fileList) {
        StringBuilder buffer = new StringBuilder(128);
        for (String file : fileList) {
            buffer.append("You successfully uploaded file=");
            buffer.append(file);
            buffer.append("<br />");
        }

        return buffer.toString();
    }

    private static String getTempFileLocation() {
        // Creating the directory to store file
        String rootPath = System.getProperty("catalina.home");
        return rootPath + File.separator + "tmpFiles";
    }

    private File getDirectory(String pathname) {
        final File dir = new File(pathname);
        if (!dir.exists()) {
            final boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                logger.warn("{} dir make fail.", pathname);
            }
        }
        return dir;
    }

    @RequestMapping(value = "/uploadView")
    public String uploadView() {
        return "fileUpload/upload";
    }

    @RequestMapping(value = "/uploadMultipleView")
    public String uploadMultipleView() {
        return "fileUpload/uploadMultiple";
    }
}
