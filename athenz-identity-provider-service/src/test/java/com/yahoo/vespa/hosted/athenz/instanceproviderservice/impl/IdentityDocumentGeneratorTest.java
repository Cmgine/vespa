package com.yahoo.vespa.hosted.athenz.instanceproviderservice.impl;


import com.google.common.collect.ImmutableSet;
import com.yahoo.component.Version;
import com.yahoo.config.provision.ApplicationId;
import com.yahoo.config.provision.ApplicationName;
import com.yahoo.config.provision.ClusterMembership;
import com.yahoo.config.provision.Environment;
import com.yahoo.config.provision.InstanceName;
import com.yahoo.config.provision.NodeType;
import com.yahoo.config.provision.RegionName;
import com.yahoo.config.provision.SystemName;
import com.yahoo.config.provision.TenantName;
import com.yahoo.config.provision.Zone;
import com.yahoo.vespa.hosted.athenz.instanceproviderservice.AthenzInstanceProviderServiceTest.AutoGeneratedKeyProvider;
import com.yahoo.vespa.hosted.athenz.instanceproviderservice.config.AthenzProviderServiceConfig;
import com.yahoo.vespa.hosted.athenz.instanceproviderservice.impl.model.ProviderUniqueId;
import com.yahoo.vespa.hosted.athenz.instanceproviderservice.impl.model.SignedIdentityDocument;
import com.yahoo.vespa.hosted.provision.Node;
import com.yahoo.vespa.hosted.provision.NodeRepository;
import com.yahoo.vespa.hosted.provision.node.Allocation;
import com.yahoo.vespa.hosted.provision.node.Generation;
import com.yahoo.vespa.hosted.provision.testutils.MockNodeFlavors;
import org.junit.Test;

import java.util.HashSet;
import java.util.Optional;

import static com.yahoo.vespa.hosted.athenz.instanceproviderservice.AthenzInstanceProviderServiceTest.getAthenzProviderConfig;
import static com.yahoo.vespa.hosted.athenz.instanceproviderservice.AthenzInstanceProviderServiceTest.getZoneConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author valerijf
 */
public class IdentityDocumentGeneratorTest {
    private static final Zone ZONE = new Zone(SystemName.cd, Environment.dev, RegionName.from("us-north-1"));

    @Test
    public void generates_valid_identity_document() throws Exception {
        String hostname = "x.y.com";

        ApplicationId appid = ApplicationId.from(
                TenantName.from("tenant"), ApplicationName.from("application"), InstanceName.from("default"));
        Allocation allocation = new Allocation(appid,
                ClusterMembership.from("container/default/0/0", Version.fromString("1.2.3")),
                Generation.inital(),
                false);
        Node n = Node.create("ostkid",
                ImmutableSet.of("127.0.0.1"),
                new HashSet<>(),
                hostname,
                Optional.empty(),
                new MockNodeFlavors().getFlavorOrThrow("default"),
                NodeType.tenant)
                .with(allocation);

        NodeRepository nodeRepository = mock(NodeRepository.class);
        when(nodeRepository.getNode(eq(hostname))).thenReturn(Optional.of(n));
        AutoGeneratedKeyProvider keyProvider = new AutoGeneratedKeyProvider();

        String dnsSuffix = "vespa.dns.suffix";
        AthenzProviderServiceConfig config = getAthenzProviderConfig("domain", "service", dnsSuffix, ZONE);
        IdentityDocumentGenerator identityDocumentGenerator = new IdentityDocumentGenerator(
                config,
                getZoneConfig(config, ZONE),
                nodeRepository,
                ZONE,
                keyProvider);
        String rawSignedIdentityDocument = identityDocumentGenerator.generateSignedIdentityDocument(hostname);


        SignedIdentityDocument signedIdentityDocument =
                Utils.getMapper().readValue(rawSignedIdentityDocument, SignedIdentityDocument.class);

        // Verify attributes
        assertEquals(hostname, signedIdentityDocument.identityDocument.instanceHostname);

        String environment = "dev";
        String region = "us-north-1";
        String expectedZoneDnsSuffix = environment + "-" + region + "." + dnsSuffix;
        assertEquals(expectedZoneDnsSuffix, signedIdentityDocument.dnsSuffix);

        ProviderUniqueId expectedProviderUniqueId =
                new ProviderUniqueId("tenant", "application", environment, region, "default", "default", 0);
        assertEquals(expectedProviderUniqueId, signedIdentityDocument.identityDocument.providerUniqueId);

        // Validate signature
        assertTrue("Message", InstanceValidator.isSignatureValid(keyProvider.getPublicKey(0),
                signedIdentityDocument.rawIdentityDocument,
                signedIdentityDocument.signature));
    }
}