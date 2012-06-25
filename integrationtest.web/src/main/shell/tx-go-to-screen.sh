#!/bin/sh
#
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
#

print_usage()
{
        echo "usage: tx-go-to-screen [<path/to/screenrc>]"
        exit 0
}

SCREENRC = `pwd`/testing-screenrc

if [ $# -lt 2 ]
then    
        if [ $# -eq 1 ]
        then
                case $1 in
                        -*)  print_usage; exit 1;;                      
                        *)   SCREENRC=$1;;
                esac
        fi
else
        echo "Too many arguments!"
        print_usage
        exit 1
fi

for screen_instance in `screen -ls | sed '1d;/Socket/d;$d;s/^.*\.//;s/(.*$//'`
do
	if [ $screen_instance = "testing" ]
	then
		screen -raD testing
		exit 0
	fi
done

`screen -c $SCREENRC -S testing`
