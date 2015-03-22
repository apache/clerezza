/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

/////////////////////////  Logging
//
//bitmask levels
TNONE = 0; 
TERROR = 1;
TWARN = 2;
TMESG = 4;
TSUCCESS = 8;
TINFO = 16;
TDEBUG = 32;
TALL = 63;
logging = {
    ascending : false, 
    level : TERROR | TWARN //default
};

function escapeForXML(str) {
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;')
}

// t is for tabulator
//log a message where type is mesg|warning|error|debug|info|good
function tmsg(msg)  { tlog(msg, TMESG, 'mesg') };
function tinfo(msg)  { tlog(msg, TINFO, 'info') };
function twarn(msg) { tlog(msg, TWARN, 'warn') };
function terror(msg) { tlog(msg, TERROR, 'eror') };
function tdebug(msg) { tlog(msg, TDEBUG, 'dbug') };
function tsuccess(msg) { tlog(msg, TSUCCESS, 'good') };
function tlog(str, type, typestr)
{
    if (!type) { type = TMESG; typestr = 'mesg'};
    if (!(logging.level & type)) return; //bitmask
    var log_area = document.getElementById('status');
    
    var addendum = document.createElement("span");
    addendum.setAttribute('class', typestr);
    var now = new Date();
    addendum.innerHTML = now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds()
            + " [" + typestr + "] "+ escapeForXML(str) + "<br/>";
    if (logging.ascending)
        log_area.appendChild(addendum);
    else
        log_area.insertBefore(addendum, log_area.firstChild);
} //tlog
fyi = tdebug;
log = {}
log.msg = tmsg
log.warn = twarn
log.debug = tdebug
log.info = tinfo
log.error = terror
log.success = tsuccess

/** clear the log window **/
function clearStatus(str) {
    var x = document.getElementById('status');
    if (!x) return;
    //x.innerHTML = "";
    emptyNode(x);
} //clearStatus

/** set the logging level **/
function setLogging(x) {
    logging.level = TALL;
    fyi("Logging "+x);
    logging.level = x;
}

function dumpStore() {
    var l = logging.level;
    logging.level = TALL;
    fyi("\nStore:\n" + kb + "__________________\n");
    tdebug("subject index: " + kb.subjectIndex[0] + kb.subjectIndex[1]);
    tdebug("object index: " + kb.objectIndex[0] + kb.objectIndex[1]);
    logging.level = l;
}

function dumpHTML() {
    var l = logging.level;
    logging.level = TALL;
    fyi(document.innerHTML);
    logging.level = l
}
