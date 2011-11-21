in the Clerezza console run (in this same order):

   :f start mvn:org.apache.clerezza/uima.utils
   :f start mvn:org.apache.clerezza/uima.ontologies
   :f start mvn:org.apache.clerezza/uima.ontologies.ao
   :f start mvn:org.apache.clerezza/uima.casconsumer
   :f start mvn:org.apache.clerezza/uima.samples

then try sending the following HTTP POST with cURL:
   curl -d "uri=http://people.apache.org/~tommaso/projects.html" http://localhost:8080/uima/opennlp/person
