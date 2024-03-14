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

import com.google.gson.JsonObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

public class Log {

    public static String authorization(
            final String identity,
            final HashSet<String> indexes,
            final String container,
            final String blob,
            final Boolean success) {
        String outcome;

        if (success) {
            outcome = "OK";
        }
        else {
            outcome = "NOK";
        }

        // request type_info
        JsonObject typeInfo = new JsonObject();
        typeInfo.addProperty("request_id", "");
        typeInfo.addProperty("session_id", "");
        typeInfo.addProperty("subject", identity);
        typeInfo.addProperty("predicate", "GRANT");
        typeInfo.addProperty("object", indexes.toString());
        typeInfo.addProperty("outcome", outcome);

        // content
        JsonObject content = new JsonObject();
        content.addProperty("container", container);
        content.addProperty("blob", blob);

        // common info
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp",
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                        .format(new Date()));
        jsonObject.addProperty("version", "1");
        jsonObject.addProperty("application", "teragrep");
        jsonObject.addProperty("environment", "");
        jsonObject.addProperty("component", "pth_05");
        try {
            jsonObject.addProperty("instance", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            jsonObject.addProperty("instance","");
        }
        jsonObject.addProperty("retention", "");
        jsonObject.addProperty("uuid", UUID.randomUUID().toString());
        jsonObject.addProperty("type", "authorization");
        jsonObject.add("type_info", typeInfo);

        jsonObject.add("content", content);



        return jsonObject.toString();
    }
}
