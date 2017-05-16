#!/usr/bin/python
from kazoo.client import KazooClient
import logging
import re
import subprocess
import string
import json
import urllib2
import time

logging.basicConfig()
zk_key="/flink/cluster_flink/leader"
zk = KazooClient(hosts='dev.zk.pinpoint.navercorp.com', read_only=True)
zk.start()

if zk.exists("/flink/cluster_flink/leader"):
    data, stat = zk.get(zk_key)
    match = re.search(r"@(.*?):", data)
    leaderIpAddr = match.group(1)
    print "Leader JobManager IP Addr :",leaderIpAddr
    hostIpAddr = subprocess.Popen(['hostname', '-I'], stdout=subprocess.PIPE).communicate()[0].rstrip()
    print "Host Name :",hostIpAddr
    if hostIpAddr == leaderIpAddr :
        url = "http://%s:8081/joboverview/running" % leaderIpAddr
        jobInfo = json.load(urllib2.urlopen(url))
        if jobInfo["jobs"] :
            print "Job exists. Try to cancel job.",jobInfo["jobs"]
            jobId = jobInfo["jobs"][0]["jid"]
            output_stop = subprocess.Popen(['/home1/irteam/apps/flink/bin/flink', 'cancel', jobId, '-m', leaderIpAddr + ':50000'], stdout=subprocess.PIPE).communicate()[0]
            time.sleep(5)
            print output_stop
        output_submit = subprocess.Popen(['/home1/irteam/apps/flink/bin/flink', 'run', '-d', '-p', '15', '-m', leaderIpAddr + ':50000', '/home1/irteam/deploy/doc_base/pinpoint-flink-job-2.0.jar'], stdout=subprocess.PIPE).communicate()[0]
        print output_submit
    else :
        print ">>>>>>>>>This host is not leader!>>>>>>>>>"

zk.stop()
