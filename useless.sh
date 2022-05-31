# run_servers.sh
#!/bin/bash

HOSTS="35.172.235.46"

IDX=0
for HOSTNAME in ${HOSTS} ; do 
    #rsync -auvz -e "ssh -i ~/.ssh/$USER-keypair" /home/maydogdu/bowdoin-coin $USER@${HOSTNAME}:~
    #scp -i ~/.ssh/$USER-keypair -r /home/maydogdu/bowdoin-coin $USER@${HOSTNAME}:~/bowdoin-coin
    #ssh -i ~/.ssh/$USER-keypair $USER@${HOSTNAME} "pkill -u $USER -f '^java Driver 8100$'"
    ssh -i ~/.ssh/$USER-keypair $USER@${HOSTNAME} "cd bowdoin-coin && java Driver 8100" &
    #ssh -i ~/.ssh/$USER-keypair $USER@${HOSTNAME} "joinNode--52.90.4.149--8100"
    echo "transferred ${IDX}"
    let IDX=${IDX}+1
done
