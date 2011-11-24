package cgimp

import org.springframework.context.*

class GatherJob {

    // def timeout = 5000l // execute job once in 5 seconds
    def concurrent = false
    ApplicationContext applicationContext

    static triggers = {
      cron name:'cronTrigger', startDelay:10000, cronExpression: "0 0/2 * * * ?"
      // simple name:'simpleTrigger', startDelay:10000, repeatInterval: 30000, repeatCount: 10
      // cron name:'cronTrigger', startDelay:10000, cronExpression: '0/6 * 15 * * ?'
      // custom name:'customTrigger', triggerClass:MyTriggerClass, myParam:myValue, myAnotherParam:myAnotherValue
    }

    def execute() {

      def start_time = System.currentTimeMillis();

      // execute task
      log.debug("Execute gatherer job starting at ${new Date()}");
      com.k_int.gatherer.Agent.findAll().each { agent ->
        log.debug("Processing agent ${agent.agentName}");
        Class clazz = new GroovyClassLoader(this.class.getClassLoader()).parseClass(agent.agentCode);
        def ai = clazz.newInstance();
        def props = [:]
        ai.process(props, applicationContext)
      }

      def elapsed = System.currentTimeMillis() - start_time;

      log.debug("GatherJob completed in ${elapsed}ms at ${new Date()}");
    }
}
