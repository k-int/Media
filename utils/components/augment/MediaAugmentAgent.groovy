#!/usr/bin/groovy

@GrabResolver(name='es', root='https://oss.sonatype.org/content/repositories/releases')
// @Grab(group='com.gmongo', module='gmongo', version='0.9.2')
@Grapes([
  @Grab(group='org.apache.httpcomponents', module='httpmime', version='4.1.2'),
  @Grab(group='com.gmongo', module='gmongo', version='0.9.2'),
  @Grab(group='org.apache.httpcomponents', module='httpclient', version='4.0'),
  @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.0')
])

def starttime = System.currentTimeMillis();
println("Startup.. Initialising at ${starttime}");

def monitor = new RepoMonitor();
def tse = new TripleStoreService();
def reccount = 0;

println("initialise triple store");
tse.init();

println("Connect to mongo");
def mongo = new com.gmongo.GMongo()
def db = mongo.getDB("frbr")

def image_repo_dir = "${System.getProperty('user.home')}/media/images"
println("WOrking with image repo dir ${image_repo_dir}");

File image_repo_dir_file = new File(image_repo_dir);
image_repo_dir_file.mkdirs();

println("Monitor starting after ${System.currentTimeMillis() - starttime}");

monitor.iterateLatest(db,'work', -1) { jsonobj ->
  
  println("Process [${reccount++}] ${jsonobj._id}");
  def remote_image_url = jsonobj.expressions[0].manifestations[0].uri
  println("Fetch image from : ${remote_image_url}");
 

  // def writer = new StringWriter()
  // def xml = new groovy.xml.MarkupBuilder(writer)
  // xml.setOmitEmptyAttributes(true);
  // xml.setOmitNullAttributes(true);
  // xml.'xcri:provider'('rdf:about':"urn:xcri:provider:${jsonobj._id}",
  //                     'xmlns:rdf':'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
  //                     'xmlns:dcterms':'http://purl.org/dc/terms/',
  //                     'xmlns:dc':'http://purl.org/dc/elements/1.1/',
  //                     'xmlns:xcri':'http://xcri.org/profiles/catalog/1.2/') {
  //   'dc:title'(jsonobj.label)
  // }


  println("Checking for any existing items where workId matches");
  def item_record = null;
  item_record = db.item.findOne(workId:jsonobj._id);
  if ( item_record == null ) {
    println("Create new item record");
    item_record = [:]
    item_record._id = new org.bson.types.ObjectId();
  }
  else {
    println("update existing item record");
  }

  item_record.originalSource = [type:'external',uri:remote_image_url]
  item_record.workId = jsonobj._id;
  item_record.workflowType = 'master'
  item_record.mimeType = 'application/jpg'

  // Read the remote image file into the local file
  def output_filename = "${image_repo_dir}/${item_record._id.toString()}"
  def out_file = new FileOutputStream(output_filename)
  def out_stream = new BufferedOutputStream(out_file)
  out_stream << new URL(jsonobj.expressions[0].manifestations[0].uri).openStream()
  out_stream.close()
  // db.item.save(item_record);

  println("New item has id ${item_record._id} and saved in ${output_filename}");

  // def result = writer.toString();
  // tse.removeGraph("urn:xcri:course:${jsonobj._id}");
  // tse.update(result, "urn:xcri:course:${jsonobj._id}", 'application/rdf');
  // println("Updated provider ( ${System.currentTimeMillis() - starttime} )");
}

println("Completed after ${reccount} records in ${System.currentTimeMillis() - starttime}ms");
