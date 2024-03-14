/*
 * S3 Authorization enabled object gateway service pth_05
 * Copyright (C) 2021  Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://github.com/teragrep/teragrep/blob/main/LICENSE>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */

package com.teragrep.pth_05.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Module;
import com.teragrep.pth_05.TeragrepBlobStoreLocator;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.concurrent.DynamicExecutors;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class TeragrepBlobStoreLocatorTest {
    @Test
    public void testLocateBlobStore() throws IOException {
        TeragrepBlobStoreLocator blobStoreLocator = new TeragrepBlobStoreLocator("src/test/resources/credentials.json", "src/test/resources/authorize.json", "src/test/resources/lookup");

        Properties properties = new Properties();
        properties.setProperty("jclouds.filesystem.basedir", "/tmp/testLocateBlobStore");

        final String provider = "filesystem";

        ThreadFactory factory = new ThreadFactoryBuilder()
                .setNameFormat("test thread %d")
                .setThreadFactory(Executors.defaultThreadFactory())
                .build();

        ExecutorService executorService = DynamicExecutors.newScalingThreadPool(
                1, 20, 60 * 1000, factory);

        ContextBuilder builder = ContextBuilder
                .newBuilder(provider)
                .modules(ImmutableList.<Module>of(
                        new SLF4JLoggingModule(),
                        new ExecutorServiceModule(executorService)))
                .overrides(properties);

        BlobStoreContext context = builder.build(BlobStoreContext.class);

        blobStoreLocator.setBlobStore(context.getBlobStore());

        Map.Entry<String, BlobStore> credentialToBlobStore = blobStoreLocator.
                locateBlobStore(
                        "root" ,
                        "100year-bucket",
                        "2021/07-22/testhost/testtag/testtag.log-2021072210.log.gz"
                );

        Assertions.assertEquals("aP051Xd3f1n3d@account", credentialToBlobStore.getKey());
        Assertions.assertNotNull(credentialToBlobStore.getValue());
    }


    @Test
    public void failLocateBlobStore() throws IOException {
        TeragrepBlobStoreLocator blobStoreLocator = new TeragrepBlobStoreLocator("src/test/resources/credentials.json", "src/test/resources/authorize.json", "src/test/resources/lookup");

        Properties properties = new Properties();
        properties.setProperty("jclouds.filesystem.basedir", "/tmp/testLocateBlobStore");

        final String provider = "filesystem";

        ThreadFactory factory = new ThreadFactoryBuilder()
                .setNameFormat("test thread %d")
                .setThreadFactory(Executors.defaultThreadFactory())
                .build();

        ExecutorService executorService = DynamicExecutors.newScalingThreadPool(
                1, 20, 60 * 1000, factory);

        ContextBuilder builder = ContextBuilder
                .newBuilder(provider)
                .modules(ImmutableList.<Module>of(
                        new SLF4JLoggingModule(),
                        new ExecutorServiceModule(executorService)))
                .overrides(properties);

        BlobStoreContext context = builder.build(BlobStoreContext.class);

        blobStoreLocator.setBlobStore(context.getBlobStore());

        Map.Entry<String, BlobStore> credentialToBlobStore = blobStoreLocator.
                locateBlobStore(
                        "nobody" ,
                        "5year-bucket",
                        "2021/07-22/testhost/testtag/testtag.log-2021072210.log.gz"
                );

        Assertions.assertNull(credentialToBlobStore);
    }
}
