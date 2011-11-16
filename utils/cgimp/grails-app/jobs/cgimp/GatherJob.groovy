package cgimp

class GatherJob {

    // def timeout = 5000l // execute job once in 5 seconds
    def concurrent = false

    static triggers = {
      cron name:'cronTrigger', startDelay:10000, cronExpression: "0 0/2 * * * ?"
      // simple name:'simpleTrigger', startDelay:10000, repeatInterval: 30000, repeatCount: 10
      // cron name:'cronTrigger', startDelay:10000, cronExpression: '0/6 * 15 * * ?'
      // custom name:'customTrigger', triggerClass:MyTriggerClass, myParam:myValue, myAnotherParam:myAnotherValue
    }

    def execute() {
      // execute task
      log.debug("Execute gatherer job");
      com.k_int.gatherer.Agent.findAll().each { agent ->
        log.debug("Processing agent ${agent.agentName}");
      }
      log.debug("GatherJob completed");
    }
}
