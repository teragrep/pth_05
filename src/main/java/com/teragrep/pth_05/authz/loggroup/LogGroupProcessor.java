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

package com.teragrep.pth_05.authz.loggroup;

import com.google.gson.Gson;
import com.teragrep.pth_05.authz.RequestAuthorizer;
import com.teragrep.pth_05.authz.loggroup.lookup.LookupTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class LogGroupProcessor {
    private static final Logger logger = LoggerFactory.getLogger(
            LogGroupProcessor.class);

    final Gson gson;

    public LogGroupProcessor() {
        this.gson = new Gson();
    }

    public LinkedList<LogGroup> load(String path) throws IOException {
        final LinkedList<LogGroup> logGroupList = new LinkedList<>();
        Set<String> groups;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path))) {
            LogGroupSearch groupSearch = new LogGroupSearch(stream);
            groups = groupSearch.getGroupList();

        }

        //logger.debug("Found log group names <[" + groups + "]>");

        if (groups != null && groups.size() > 0) {

            for (String group : groups) {
                // read hosts for the Group
                final Set<String> hosts = loadHostsFile(path, group);

                // read indexes for the Group
                final HashMap<String, String> tag2index = loadIndexesFile(path, group);

                logGroupList.add(new LogGroup(group, hosts, tag2index));
            }
        }

        //logger.debug("Loaded log group list <[" + logGroupList + "]>");

        return logGroupList;
    }

    private Set<String> loadHostsFile(String path, String group) throws FileNotFoundException {
        BufferedReader hostReader = new BufferedReader(
                new FileReader(path + "/" + group + "_hosts.json"));
        LookupTable hostsTable = gson.fromJson(hostReader, LookupTable.class);

        final Set<String> groupHosts = new HashSet<>();
        for (int i = 0; i < hostsTable.getTable().size(); i++) {
            if ("true".equals(hostsTable.getTable().get(i).getValue()))
                groupHosts.add(hostsTable.getTable().get(i).getIndex().toLowerCase(Locale.ROOT));
        }
        return groupHosts;
    }

    private HashMap<String, String> loadIndexesFile(String path, String group) throws FileNotFoundException {
        BufferedReader indexesReader = new BufferedReader(
                new FileReader(path + "/" + group + "_indexes.json"));
        LookupTable indexesTable = gson.fromJson(indexesReader, LookupTable.class);

        final HashMap<String,String> groupIndexes = new HashMap<>();
        for (int i = 0; i < indexesTable.getTable().size(); i++) {
            groupIndexes.put(
                    indexesTable.getTable().get(i).getIndex().toLowerCase(Locale.ROOT), // tag
                    indexesTable.getTable().get(i).getValue().toLowerCase(Locale.ROOT) // index
                    );
        }
        return groupIndexes;
    }
}
