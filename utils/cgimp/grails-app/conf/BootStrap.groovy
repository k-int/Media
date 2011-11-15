import org.springframework.core.io.Resource
import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.apache.shiro.crypto.hash.Sha256Hash
import grails.util.GrailsUtil


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

          // log.debug("Loading handler: ${h.getHandlerName()} revision: ${h.getRevision()}, preconditions: ${h.getPreconditions()}");
          // def nh = Handler.findByName(h.getHandlerName()) ?: new Handler(name:h.getHandlerName(), preconditions:h.getPreconditions()).save()
          // def nr = new HandlerRevision(owner:nh,
          //                              revision:h.getRevision(),
          //                              handler:handler_file.text).save();
        }
      }


    }

    def destroy = {
    }
}
