svn co https://svn.apache.org/repos/asf/clerezza/site/production/ /tmp/site-production
curl -u admin:admin "http://localhost:8080/admin/offline/download?baseUri=http://localhost:8080/&targetUri=http://clerezza.apache.org/&formatExtension=xhtml&formatExtension=rdf&formatExtension=png&formatExtension=html&formatExtension=js&formatExtension=jpeg&formatExtension=css"  > /tmp/site-production.zip
unzip -o /tmp/site-production.zip -d /tmp/site-production
cd /tmp/site-production 
svn add * --force
svn commit -m "Auto commit by site deploy script" .
