#!/usr/bin/python3

# S3 Authorization enabled object gateway service pth_05
# Copyright (C) 2021  Suomen Kanuuna Oy
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://github.com/teragrep/teragrep/blob/main/LICENSE>.
#
#
# Additional permission under GNU Affero General Public License version 3
# section 7
#
# If you modify this Program, or any covered work, by linking or combining it
# with other code, such other code is not for that reason alone subject to any
# of the requirements of the GNU Affero GPL version 3 as long as this Program
# is the same Program as licensed from Suomen Kanuuna Oy without any additional
# modifications.
#
# Supplemented terms under GNU Affero General Public License version 3
# section 7
#
# Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
# versions must be marked as "Modified version of" The Program.
#
# Names of the licensors and authors may not be used for publicity purposes.
#
# No rights are granted for use of trade names, trademarks, or service marks
# which are in The Program if any.
#
# Licensee must indemnify licensors and authors for any liability that these
# contractual assumptions impose on licensors and authors.
#
# To the extent this program is licensed as part of the Commercial versions of
# Teragrep, the applicable Commercial License may apply to this file if you as
# a licensee so wish it.

import configparser
import sys
import os
import json

# Usage
if len(sys.argv) < 2:
    print(f"Usage: python3 {sys.argv[0]} path/to/config/dir")
    sys.exit(0)

# sanity checks
config_path = sys.argv[1]
if not os.path.isdir(config_path):
    print(f"Failure: Argument given is not a directory or does not exist")
    sys.exit(1)

if not os.path.isfile(f"{config_path}/authentication.conf") or not os.path.isfile(f"{config_path}/authorize.conf"):
    print(f"Can't find authentication.conf and/or authorization.conf from '{config_path}'")
    sys.exit(1)

# Read configs
authentication = configparser.ConfigParser(strict=False)
authentication.read(f"{config_path}/authentication.conf")

authorize = configparser.ConfigParser(strict=False)
authorize.read(f"{config_path}/authorize.conf")

# Container for authentications
groups = {}
for key in authentication:
    # Ignore irrelevant
    if key.startswith("roleMap"):
        for usermap in authentication[key]:
            groups[usermap] = authentication[key][usermap]

# Find permissions for all groups
permission_list = []
for key in authorize:
    if key.removeprefix("role_") in groups and "srchIndexesAllowed" in authorize[key]:
        if len(groups[key.removeprefix("role_")]) != 0 and len(authorize[key]["srchIndexesAllowed"]) != 0:
            permission_list.append({"group": groups[key.removeprefix("role_")], "allowedIndexes": authorize[key]["srchIndexesAllowed"].split(";")})

print(json.dumps(permission_list))
