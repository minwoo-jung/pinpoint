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
package com.navercorp.pinpoint.manager.service;

import com.navercorp.pinpoint.manager.core.SchemaStatus;
import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.exception.hbase.DuplicateHbaseException;
import com.navercorp.pinpoint.manager.exception.hbase.HbaseManagementException;
import com.navercorp.pinpoint.manager.exception.hbase.UnknownHbaseException;
import com.navercorp.pinpoint.manager.vo.hbase.HbaseInfo;

import java.util.Set;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public interface HbaseManagementService {

    /**
     * Returns {@code true} if a hbase namespace with the given {@code namespace} exists.
     *
     * @param namespace hbase namespace to check for existence
     * @return {@code true} if the hbase namespace exists
     */
    boolean namespaceExists(String namespace);

    /**
     * Returns the current status of the hbase schema under the given {@code namespace}.
     *
     * @param namespace hbase namespace to check the schema status of
     * @return current status of the hbase schema
     */
    SchemaStatus getSchemaStatus(String namespace);

    /**
     * Returns the current information of the hbase namespace.
     *
     * @param namespace hbase namespace to retrieve the information from
     * @return current hbase information
     */
    HbaseInfo getHbaseInfo(String namespace);

    /**
     * Updates the current status of the hbase namespace to {@code newStatus} if the current status is one of those
     * specified in {@code validStatuses}, and returns {@code true}. Returns {@code false} if the current status is not
     * one of those specified in {@code validStatuses}, and no change is made to the current status.
     *
     * @param namespace hbase namespace to update the status
     * @param newStatus new status to update to
     * @param validStatuses valid current statuses to carry out the update
     * @return {@code true} if the status has been successfully updated, {@code false} if otherwise
     * @throws HbaseManagementException if there was a problem updating hbase status
     */
    boolean checkAndUpdateHbaseStatus(String namespace, StorageStatus newStatus, Set<StorageStatus> validStatuses);

    /**
     * Starts creating a new hbase namespace with the specified {@code namespace}, creating the tables and initializing
     * the schema to serve as a backend storage for Pinpoint.
     * Returns {@code true} if hbase namespace creation is successfully started, and {@code false} if otherwise.
     * Note that the return value of {@code true} does not signify a successful creation of the namespace.
     *
     * @param namespace hbase namespace to create
     * @return {@code true} if hbase namespace creation is successfully started
     * @throws DuplicateHbaseException if the namespace already exists in hbase
     * @throws HbaseManagementException if there was a problem updating hbase status
     */
    boolean createRepository(String namespace);

    /**
     * Starts updating the schema for the hbase namespace specified by {@code namespace}.
     * Returns {@code true} if the schema update is successfully started, and {@code false} if otherwise.
     * Note that the return value of {@code true} does not signify a successful completion of schema update.
     *
     * @param namespace hbase namespace to update the schema
     * @return {@code true} if schema update is successfully started
     * @throws UnknownHbaseException if no namespace with the given {@code namespace} exists
     * @throws HbaseManagementException if there was a problem updating hbase status
     */
    boolean updateRepository(String namespace);

    /**
     * Starts deleting the hbase namespace specified by {@code namespace}.
     * Returns {@code true} if hbase namespace deletion is successfully started, and {@code false} if otherwise.
     * Note that the return value of {@code true} does not signify a successful deletion of the namespace.
     *
     * @param namespace hbase namespace to delete
     * @return {@code true} if hbase namespace deletion is successfully started
     * @throws UnknownHbaseException if no namespace with the given {@code namespace} exists
     * @throws HbaseManagementException if there was a problem updating hbase status
     */
    boolean deleteRepository(String namespace);
}
