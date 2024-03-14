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

package com.teragrep.pth_05;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.gaul.s3proxy.AuthenticationType;
import org.gaul.s3proxy.S3Proxy;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.concurrent.DynamicExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(
            Main.class);

    public static void main(String[] args) {
        ExitCode exitCode;
        Properties properties = readPropertiesFile();

        ThreadFactory factory = new ThreadFactoryBuilder()
                .setNameFormat("user thread %d")
                .setThreadFactory(Executors.defaultThreadFactory())
                .build();

        ExecutorService executorService = DynamicExecutors.newScalingThreadPool(
                1, 20, 60 * 1000, factory);


        BlobStore blobStore = null;
        try {
            blobStore = S3ProxyBlobStoreFactory.createBlobStore(
                    properties, executorService
            );
        } catch (IOException e) {
            logger.error(e.toString());
            exitCode = ExitCode.BLOBSTORE_CREATION_ERROR;
            System.exit(exitCode.getExitCode());
        }


        S3Proxy s3Proxy = S3Proxy.builder()
                .awsAuthentication(AuthenticationType.AWS_V2_OR_V4, "", "")
                .blobStore(blobStore)
                .endpoint(URI.create(properties.getProperty("pth_05.endpoint")))
                .build();

        // our authorizing blobStoreLocator
        TeragrepBlobStoreLocator bLocator = null;
        try {
            bLocator = new TeragrepBlobStoreLocator(
                    properties.getProperty("pth_05.credentials.file"),
                    properties.getProperty("pth_05.authorize.file"),
                    properties.getProperty("pth_05.lookup.path")

            );
        } catch (IOException e) {
            logger.error(e.toString());
            exitCode = ExitCode.BLOBSTORE_LOCATOR_CREATION_ERROR;
            System.exit(exitCode.getExitCode());
        }
        bLocator.setBlobStore(blobStore);
        s3Proxy.setBlobStoreLocator(bLocator);

        try {
            s3Proxy.start();
        } catch (Exception e) {
            logger.error(e.toString());
            exitCode = ExitCode.S3PROXY_START_ERROR;
            System.exit(exitCode.getExitCode());
        }
        while (!s3Proxy.getState().equals(AbstractLifeCycle.STARTED)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                logger.warn(e.toString());
            }
        }
    }

    private static Properties readPropertiesFile() {
        ExitCode exitCode;
        Properties systemProperties = System.getProperties();
        final String pth05PropertiesFile = systemProperties.getProperty("pth_05.propertiesFile");
        if ( pth05PropertiesFile != null) {
            try {
                final FileReader reader = new FileReader(pth05PropertiesFile);
                systemProperties.load(reader);
                reader.close();
            } catch (IOException e) {
                logger.error(e.toString());
                exitCode = ExitCode.PROPERTY_PTH_05_PROPERTIES_NO_SUCH_FILE;
                System.exit(exitCode.getExitCode());
            }
        }

        // require endpoint
        if (systemProperties.getProperty("pth_05.endpoint") == null) {
            exitCode = ExitCode.PROPERTY_ENDPOINT;
            logger.error("pth_05.endpoint" + " not set, existing.");
            System.exit(exitCode.getExitCode());
        }

        // require credentials.file
        if (systemProperties.getProperty("pth_05.credentials.file") == null) {
            exitCode = ExitCode.PROPERTY_CREDENTIALS_FILE;
            logger.error("pth_05.credentials.file" + " not set, existing.");
            System.exit(exitCode.getExitCode());
        }

        // require authorize.file
        if (systemProperties.getProperty("pth_05.authorize.file") == null) {
            exitCode = ExitCode.PROPERTY_AUTHORIZE_FILE;
            logger.error("pth_05.authorize.file" + " not set, existing.");
            System.exit(exitCode.getExitCode());
        }

        // require lookup.path
        if (systemProperties.getProperty("pth_05.lookup.path") == null) {
            exitCode = ExitCode.PROPERTY_LOOKUP_PATH;
            logger.error("pth_05.lookup.path" + " not set, existing.");
            System.exit(exitCode.getExitCode());
        }

        return systemProperties;
    }
}
