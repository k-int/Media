package cgimp

import org.springframework.core.io.Resource
import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.apache.shiro.crypto.hash.Sha256Hash
import grails.util.GrailsUtil
import com.k_int.gatherer.*


class AgentManagerService {

  def loadAgents(r) {
    log.debug("Loading agents from disk cache");

    def f = r.getFile();
    log.debug("got agents dir: ${f}");

    if ( f.isDirectory() ) {
      f.listFiles().each { agent_file ->
        log.debug("Procesing ${agent_file}");

        GroovyClassLoader gcl = new GroovyClassLoader();
        // GroovyClassLoader gcl = this.class.classLoader
        Class clazz = gcl.parseClass(agent_file.text);
        Object h = clazz.newInstance();

        log.debug("Looking up agent ${h.getAgentName()}");
        Agent agent = Agent.findByAgentName(h.getAgentName())
        if ( agent == null ) {
          log.debug("Register new agent");
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
        else {
          log.debug("Agent already present");
        }

        log.debug("Loaded ${agent_file}");
      }
    }
  }

}
