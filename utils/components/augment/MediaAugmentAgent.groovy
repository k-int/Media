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
println("MEDIA Augment/Enrich Startup.. Initialising at ${starttime}");

def monitor = new RepoMonitor();
def reccount = 0;

println("initialise triple store");
def tse = new TripleStoreService();
tse.init();

println("Initialise steganogoraphy component (steghide)");
def steg = new SteghideStegHandler();

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
  item_record = db.item.findOne(workId:jsonobj._id, workflowType:'original');
  if ( item_record == null ) {
    println("Create new item record");
    item_record = [:]
    item_record._id = new org.bson.types.ObjectId();
  }
  else {
    println("update existing item record");
  }

  def output_filename = "${image_repo_dir}/${item_record._id.toString()}"

  item_record.originalSource = [type:'external',uri:remote_image_url]
  item_record.workId = jsonobj._id;
  item_record.workflowType = 'original'
  item_record.mimeType = 'application/jpg'
  item_record.pathInStore = output_filename
  item_record.createDate = System.currentTimeMillis();

  // Read the remote image file into the local file
  def out_file = new FileOutputStream(output_filename)
  def out_stream = new BufferedOutputStream(out_file)
  out_stream << new URL(jsonobj.expressions[0].manifestations[0].uri).openStream()
  out_stream.close()
  db.item.save(item_record);

  createSecureCopy(db,image_repo_dir, jsonobj, item_record);

  println("New item has id ${item_record._id} and saved in ${output_filename}");

  println("*complete*");

  // def result = writer.toString();
  // tse.removeGraph("urn:xcri:course:${jsonobj._id}");
  // tse.update(result, "urn:xcri:course:${jsonobj._id}", 'application/rdf');
  // println("Updated provider ( ${System.currentTimeMillis() - starttime} )");
}

println("Completed after ${reccount} records in ${System.currentTimeMillis() - starttime}ms");

/**
 *  Create a copy of the original item.
 *  Steg hide the ID of the new item in the image itself
 *  Add the id of the image to the exif and the XMP metadata
 *  Create the new item
 */ 
def createSecureCopy(db,image_repo_dir, work, original_item) {

  def new_item_id = new org.bson.types.ObjectId();
  def new_file_name = "${image_repo_dir}/${new_item_id}"

  def new_item = [:]
  new_item._id = new_item_id
  new_item.workId = work._id;
  new_item.workflowType = 'SecureCopy'
  new_item.createDate = System.currentTimeMillis();
  new_item.pathInStore = new_file_name

  println("Create secure copy from original... New item id is ${new_item_id}, store location will be ${new_file_name}");

  // Copy....
  def copy_cmd = "cp ${original_item.pathInStore} ${new_file_name}"
  println("copy: ${copy_cmd}");
  def process = copy_cmd.execute()

  def steg = new SteghideStegHandler();

  // Augment metadata

  // steg hide item identifier
  steg.hide(new_item_id,  new_file_name);

  // save
  db.item.save(new_item);
}
