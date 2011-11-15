import com.gmongo.GMongo

class ncmgAgent {
  
  // handlers have access to the repository mongo service.. suggest you use http://blog.paulopoiati.com/2010/06/20/gmongo-0-5-released/
  def getAgentName() {
    "Nottinhgam City Museums and Galleries Agent"
  }

  def getRevision() {
    1
  }

  def process(properties, ctx) {
    println "This is the NCMG agent code......."
  }
}
