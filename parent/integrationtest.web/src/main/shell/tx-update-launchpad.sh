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
	echo "usage: tx-update-launchpad [<output-directory> <project-uri>]"
	exit 0
}



LAUNCHPAD_DIR=`pwd`
LAUNCHPAD_URI=http://repo.trialox.org/snapshot/org/apache/clerezza/org.apache.clerezza.cms.launchpad/

if [ $# -lt 3 ]
then	
	if [ $# -ge 1 ]
	then
		case $1 in
			-*)  print_usage; exit 1;;			
			*)   LAUNCHPAD_DIR=$1;;
		esac
	fi
	if [ $# -eq 2 ]
	then
		LAUNCHPAD_URI = $2
	fi
else
	echo "Too many arguments!"
	print_usage
	exit 1
fi


echo "looking for latest launchpad version..."

for line in `curl $LAUNCHPAD_URI 2> /dev/null | sed 's/^.*\">//;s/<\/a.*$//;1,/Parent Directory/d;/^[^0-9]/,$d;'`
do
	latest_dir=$line
done

for line in `curl $LAUNCHPAD_URI$line 2> /dev/null | sed '1,/<a href=\"o/d;/<hr>/,$d;/.jar</!d;/sources/d;s/^ *<a href=\"//;s/\">.*$//'`
do
	latest_file=$line
done

echo "found: $latest_dir$latest_file"

if [ -e $LAUNCHPAD_DIR/$latest_file ]
then
	echo "the current launchpad is already the latest version."
else
	echo "downloading the latest launchpad..."
	
	oldfile=`ls -l launchpad | sed 's/^.*-> //'`

	if  wget -O $LAUNCHPAD_DIR/$latest_file $LAUNCHPAD_URI$latest_dir$latest_file
	then
		if [ -e launchpad ]
		then
			if [ "$oldfile" != "$latest_file" ]
			then
				rm -f $LAUNCHPAD_DIR/$oldfile
				unlink launchpad
			fi
		fi

		ln -s $LAUNCHPAD_DIR/$latest_file launchpad
	else
		exit 1
	fi
fi

exit 0
