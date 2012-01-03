import org.springframework.core.io.Resource
import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.apache.shiro.crypto.hash.Sha256Hash
import grails.util.GrailsUtil
import com.k_int.gatherer.*


class BootStrap {

  def agentManagerService

  def init = { servletContext ->
    def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT)
    Resource r = ctx.getResource("/WEB-INF/agents");
    agentManagerService.loadAgents(r);
  }

  def destroy = {
  }
}
