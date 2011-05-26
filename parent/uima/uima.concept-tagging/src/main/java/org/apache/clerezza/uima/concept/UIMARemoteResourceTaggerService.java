package org.apache.clerezza.uima.concept;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.uima.utils.UIMAExecutor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST service for concept tagging which accepts 'uri' parameter to tag concepts and create the graph for that resource
 */

@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/resourcetagger")
public class UIMARemoteResourceTaggerService {

  private static final String PATH = "/AggregateResourceTaggerAE.xml";

  @GET
  @Path("tag")
  @Produces("application/rdf+xml")
  public Graph tagUri(@QueryParam("uri") String uri, @QueryParam("key") String key) {

    if (uri == null || uri.length() == 0)
      throw new WebApplicationException(Response.status(
              Response.Status.BAD_REQUEST).entity(new StringBuilder("No URI specified").toString()).build());

    try {
      System.err.println(new File(PATH).toURI().toURL());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    UIMAExecutor executor = new UIMAExecutor(PATH).withResults();
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("outputgraph",uri);
    parameters.put("alchemykey", key);
    try {
      executor.analyzeDocument("/AggregateResourceTaggerAE.xml", uri, parameters);
    } catch (AnalysisEngineProcessException e) {
      e.printStackTrace();
      throw new WebApplicationException(Response.status(
              Response.Status.INTERNAL_SERVER_ERROR).entity(new StringBuilder("Failed UIMA execution on URI ").
              append(uri).append(" due to \n").append(e.getLocalizedMessage()).toString()).build());
    }
    return TcManager.getInstance().getMGraph(new UriRef(uri)).getGraph();
  }

}
