#!/bin/sh

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
