import org.springframework.core.io.Resource
import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.apache.shiro.crypto.hash.Sha256Hash
import grails.util.GrailsUtil
import com.k_int.gatherer.*


class BootStrap {

    def init = { servletContext ->
      def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)

      log.debug("Loading agents from disk cache");

      Resource r = ctx.getResource("/WEB-INF/agents");
      def f = r.getFile();
      log.debug("got agents dir: ${f}");

      if ( f.isDirectory() ) {
        f.listFiles().each { agent_file ->
          log.debug("Procesing ${agent_file}");

          GroovyClassLoader gcl = new GroovyClassLoader();
          Class clazz = gcl.parseClass(agent_file.text);
          Object h = clazz.newInstance();

          log.debug("Looking up agent ${h.getAgentName()}");
          Agent agent = Agent.findByAgentName(h.getAgentName())
          if ( agent == null ) {
            agent = new Agent(agentName:h.getAgentName(),
                              agentCode:agent_file.text,
                              lastRun:null,
                              nextDue:null,
                              interval:86400)
            if ( agent.save(flush:true) ) {
              log.debug("Agent save ok");
            }
            else {
              log.error("Problem saving agent ${agent.errors}");
            }
          }
          log.debug("Loaded ${agent_file}");
        }
      }


    }

    def destroy = {
    }
}
