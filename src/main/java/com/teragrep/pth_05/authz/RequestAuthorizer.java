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

package com.teragrep.pth_05.authz;

import com.teragrep.jai_01.IAuthorizationInfoProcessor;
import com.teragrep.jai_01.ReloadingAuthorizationInfoProcessor;
import com.teragrep.jue_01.UnixGroupSearch;
import com.teragrep.pth_05.authz.loggroup.LogGroup;
import com.teragrep.pth_05.authz.loggroup.LogGroupProcessor;
import org.jclouds.rest.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

public final class RequestAuthorizer {

    private static final Logger logger = LoggerFactory.getLogger(
            RequestAuthorizer.class);

    final LinkedList<LogGroup> logGroupList;
    final IAuthorizationInfoProcessor authorizationInfoProcessor;
    final UnixGroupSearch unixGroupSearch;

    public RequestAuthorizer(String authorizeJSON, String lookupPath) throws IOException {
        final LogGroupProcessor logGroupProcessor = new LogGroupProcessor();
        this.logGroupList = logGroupProcessor.load(lookupPath);

        this.authorizationInfoProcessor = new ReloadingAuthorizationInfoProcessor(authorizeJSON, 300);
        this.unixGroupSearch = new UnixGroupSearch();
    }

    private HashSet<String> getIndexes(String host, String tag) {
        final HashSet<String> indexes = new HashSet<>();
        // CFE-12 lookups have no way to determine all groups for host, iterating all
        for (LogGroup group : logGroupList) {
            final String index = group.getIndex(host,tag);
            if (index != null) {
                indexes.add(index);
            }
        }
        return indexes;
    }

    public void authorize(String identity, String container, String blob) throws IOException {

        final Request request = RequestPathProcessor.process(identity, blob);
        final HashSet<String> indexes = getIndexes(request.getHost(),request.getTag());
        final HashSet<String> indexesGroupSet = authorizationInfoProcessor.getGroupSetForIndexes(indexes);
        final HashSet<String> origIdentityMemberOfSet = unixGroupSearch.getGroups(identity);
        final HashSet<String> identityMemberOfSet = new HashSet<>(origIdentityMemberOfSet);

        identityMemberOfSet.retainAll(indexesGroupSet);
        if(identityMemberOfSet.size() == 0) {
            logger.info(Log.authorization(identity, indexes, container, blob, false));
            throw new AuthorizationException("Access to: [" + blob +
                    "] denied for " + "identity [" + identity +
                    "] who is member of groups <" +
                    origIdentityMemberOfSet +
                    "> but allowed groups for indexes <[" + indexes +
                    "]> are: <" + indexesGroupSet + ">");
        }
        else {
            logger.info(Log.authorization(identity, indexes, container, blob, true));
        }
    }
}
