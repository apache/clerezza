Editing and deploying the clerezza website

- start the webiste luancher, provisioning/launchers/website-launcher

- Go to http://localhost:8080/graph/upload-form and upload graph.nt to the content graph

- change the site as needed, do not create pages ending with /, create a page ending with /index instead, the entry page is http://localhost:8080/index (you may add "?mode=edit" to edit a page)

- retrieve the modified graph: curl 'http://localhost:8080/graph?name=urn%3Ax-localinstance%3A%2Fcontent.graph' -H 'Accept: application/n-triples' > graph.nt

- commit the changed graph.nt to version control

- execute the deploy.sh script (this requires a unix style system and will typically take several minutes)
