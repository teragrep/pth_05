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

import com.google.common.collect.Maps;
import com.teragrep.jai_02.ICredentialLookup;
import com.teragrep.jai_02.ReloadingCredentialLookup;
import com.teragrep.pth_05.authz.RequestAuthorizer;
import org.gaul.s3proxy.BlobStoreLocator;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.rest.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class TeragrepBlobStoreLocator implements BlobStoreLocator {

    private static final Logger logger = LoggerFactory.getLogger(
            TeragrepBlobStoreLocator.class);

    /**
     * BlobStoreLocator is used to find relevant blobstore,
     * and the EXPECTED AWS SIGNATURE for the current
     * request. BlobStoreLocator gives out the credential
     * during the process which then is used to construct
     * the EXPECTED AWS SIGNATURE. Then the EXPECTED AWS SIGNATURE
     * is compared with the one within the request,
     * and only if they match the blobstore is used.
     * The BlobStoreLocator uses the identity to find the
     * relevant blobstore and the relevant credential for it.
     */

    BlobStore blobStore = null;
    final ICredentialLookup credentialLookup;
    final RequestAuthorizer requestAuthorizer;

    public TeragrepBlobStoreLocator(String credentialsJSON, String authorizeJSON, String lookupPath) throws IOException {
        this.credentialLookup = new ReloadingCredentialLookup(credentialsJSON, 300);
        requestAuthorizer = new RequestAuthorizer(authorizeJSON, lookupPath);
    }


    public Map.Entry<String, BlobStore> locateBlobStore(String identity,
                                                        String container,
                                                        String blob) {
        /*
        identity: bogus-identity
        container: foobucket
        blob: asd/das/zoom.jpg
         */
        Map.Entry<String, BlobStore> cred2blobstore = null;

        if (identity != null && blob != null) {
            final String credential = credentialLookup.getCredential(identity);
            if (credential != null) {
                try {
                    requestAuthorizer.authorize(identity, container, blob);
                    cred2blobstore = Maps.immutableEntry(credential, blobStore);
                } catch (IOException | AuthorizationException e) {
                    logger.error(e.toString());
                }
            }
        }
        return cred2blobstore;
    }

    public void setBlobStore(BlobStore blobStore) {
        this.blobStore = blobStore;
    }
}
