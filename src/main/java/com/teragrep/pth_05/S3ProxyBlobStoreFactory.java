package com.teragrep.pth_05;

// From: org.gaul.s3proxy.Main, was private, now public

/*
 * Copyright 2014-2020 Andrew Gaul <andrew@gaul.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.inject.Module;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.JcloudsVersion;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.location.reference.LocationConstants;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.swift.v1.blobstore.RegionScopedBlobStoreContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutorService;


public class S3ProxyBlobStoreFactory {
        public static BlobStore createBlobStore(Properties properties,
                                                 ExecutorService executorService) throws IOException {
            String provider = properties.getProperty(Constants.PROPERTY_PROVIDER);
            String identity = properties.getProperty(Constants.PROPERTY_IDENTITY);
            String credential = properties.getProperty(
                    Constants.PROPERTY_CREDENTIAL);
            String endpoint = properties.getProperty(Constants.PROPERTY_ENDPOINT);
            properties.remove(Constants.PROPERTY_ENDPOINT);
            String region = properties.getProperty(
                    LocationConstants.PROPERTY_REGION);

            if (provider == null) {
                System.err.println(
                        "Properties file must contain: " +
                                Constants.PROPERTY_PROVIDER);
                System.exit(1);
            }

            if (provider.equals("filesystem") || provider.equals("transient")) {
                // local blobstores do not require credentials
                identity = Strings.nullToEmpty(identity);
                credential = Strings.nullToEmpty(credential);
            } else if (provider.equals("google-cloud-storage")) {
                File credentialFile = new File(credential);
                if (credentialFile.exists()) {
                    credential = Files.asCharSource(credentialFile,
                            StandardCharsets.UTF_8).read();
                }
                properties.remove(Constants.PROPERTY_CREDENTIAL);
            }

            if (identity == null || credential == null) {
                System.err.println(
                        "Properties file must contain: " +
                                Constants.PROPERTY_IDENTITY + " and " +
                                Constants.PROPERTY_CREDENTIAL);
                System.exit(1);
            }

            properties.setProperty(Constants.PROPERTY_USER_AGENT,
                    String.format("s3proxy/%s jclouds/%s java/%s",
                            Main.class.getPackage().getImplementationVersion(),
                            JcloudsVersion.get(),
                            System.getProperty("java.version")));

            ContextBuilder builder = ContextBuilder
                    .newBuilder(provider)
                    .credentials(identity, credential)
                    .modules(ImmutableList.<Module>of(
                            new SLF4JLoggingModule(),
                            new ExecutorServiceModule(executorService)))
                    .overrides(properties);
            if (!Strings.isNullOrEmpty(endpoint)) {
                builder = builder.endpoint(endpoint);
            }

            BlobStoreContext context = builder.build(BlobStoreContext.class);
            BlobStore blobStore;
            if (context instanceof RegionScopedBlobStoreContext &&
                    region != null) {
                blobStore = ((RegionScopedBlobStoreContext) context)
                        .getBlobStore(region);
            } else {
                blobStore = context.getBlobStore();
            }
            return blobStore;
        }
}
