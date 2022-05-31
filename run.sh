# run_servers.sh
#!/bin/bash

HOSTS="35.172.235.46 44.199.229.51 3.22.185.101 
18.191.134.62 13.57.194.105 54.177.19.64 34.222.143.244 
54.202.50.11 13.245.182.179 13.246.6.180 18.166.176.112 
16.162.137.92 108.136.41.214 13.233.255.217
 15.206.211.195 15.152.35.76 13.208.42.124 13.125.213.112 
 52.79.85.82 18.136.203.66 54.251.84.92 3.104.66.60 3.26.227.87 
 18.183.60.155 54.95.115.193 3.99.158.136 3.98.96.39 3.122.191.72 
 3.73.75.196 34.244.200.204 3.250.224.218 18.130.129.70"


IDX=0
for HOSTNAME in ${HOSTS} ; do 
    ssh -i ~/.ssh/$USER-keypair $USER@${HOSTNAME} "cd bowdoin-coin && java Driver 8100" &
    echo "ran ${IDX}"
    let IDX=${IDX}+1
done

