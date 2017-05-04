// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.orchestrator.model;

import com.yahoo.vespa.applicationmodel.HostName;
import com.yahoo.vespa.orchestrator.status.HostStatus;

import java.util.List;

/**
 * The API a Policy has access to
 */
public interface ApplicationApi {
    String applicationInfo();

    List<ClusterApi> getClusters();

    void setHostState(HostName hostName, HostStatus status);
    List<HostName> getNodesInGroupWithStatus(HostStatus status);

    List<StorageNode> getUpStorageNodesInGroupInClusterOrder();
    List<StorageNode> getStorageNodesAllowedToBeDownInGroupInReverseClusterOrder();
}