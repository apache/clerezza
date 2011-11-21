in the Clerezza console run (in this same order):

   :f start mvn:org.apache.clerezza/uima.utils
   :f start mvn:org.apache.clerezza/uima.ontologies
   :f start mvn:org.apache.clerezza/uima.ontologies.ao
   :f start mvn:org.apache.clerezza/uima.casconsumer
   :f start mvn:org.apache.opennlp/opennlp-tools/1.5.2-incubating
   :f start mvn:org.apache.opennlp/opennlp-maxent/3.0.2-incubating
   :f start mvn:org.apache.clerezza/uima.samples

then try sending the following HTTP POST with cURL:
   curl -d "uri=http://wwww.apache.org" http://localhost:8080/uima/regex
