*****************************
WARNING

This is an unfinished version designed to work with the trunk version of clerezza for the current version of the site use

https://svn.apache.org/repos/asf/clerezza/site/trunk/

******************************


Editing and deploying the clerezza website

- start clerezza on localhost:8080

- install the directory with the site project (the directory containing this readme) with: Dev load LocationSpec("/path/to/apache/clerezza/trunk/site", noFastUpdate)

- you'll now see the contents of the clerezza site on your local instance, the content-graph is regularly written to the graph.nt file

- change the site as needed, do not create pages ending with /, create a page ending with /index instead, the entry page is http://localhost:8080/index (you may open http://localhost:8080/tools/editor with firefox to edit it)

- commit the changed graph.nt to subversion

- execute the deploy.sh script
