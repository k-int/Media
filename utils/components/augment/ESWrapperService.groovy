import org.elasticsearch.groovy.node.GNode
import org.elasticsearch.groovy.node.GNodeBuilder
import static org.elasticsearch.groovy.node.GNodeBuilder.*


class ESWrapperService {

  def gNode = null;

  @javax.annotation.PostConstruct
  def init() {
    // log.debug("Init");

    // System.setProperty("java.net.preferIPv4Stack","true");
    // log.debug("Attempting to create a transport client...");
    // Map<String,String> m = new HashMap<String,String>();
    // m.put("cluster.name","aggr");
    // Settings s = ImmutableSettings.settingsBuilder() .put(m).build();
    // TransportClient client = new TransportClient(s);

    // If there is a aggr.dev.es.cluster=iidevaggr setm us it, otherwise cluster name is aggr

    def clus_nm = "iidevaggr"

    // log.info("Using ${clus_nm} as cluster name...");

    def nodeBuilder = new org.elasticsearch.groovy.node.GNodeBuilder()

    // log.debug("Construct node settings");

    nodeBuilder.settings {
      node {
        client = true
      }
      cluster {
        name = clus_nm
      }
      http {
        enabled = false
      }
    }

    // log.debug("Constructing node...");
    gNode = nodeBuilder.node()

    // log.debug("Init completed");
  }

  @javax.annotation.PreDestroy
  def destroy() {
    // log.debug("Destroy");
    gNode.close()
    // log.debug("Destroy completed");
  }

  def getNode() {
    // log.debug("getNode()");
    gNode
  }

}
