class RepoMonitor {

  def iterateLatest(db, collection, max_iterations, processing_closure) {

    println("iterateLatest");

    // Lookup a monitor record for the identified collection
    // Create one if it doesn't exist.
    def monitor_info = null
    def mq = db.monitors.find(coll:collection);
    if ( mq.size() == 0 ) {
      println("Create new monitor info for ${collection}");
      monitor_info = [:]
      monitor_info.coll = collection
      monitor_info.maxts = 0;
      monitor_info.maxid = null;
      def sr = db.monitors.save(monitor_info);
      println("Result of save: ${sr}");
      // Look up the new monitor, so we have one with an _id set.
      monitor_info = db.monitors.findOne(coll:collection);
    }
    else if ( mq.size() == 1 ) {
      monitor_info = mq[0];
    }
    else {
      println("Multiple entries found for collection. exit.");
      System.exit(1);
    }

    def next=true;  
    def batch_size = 10;
    def iteration_count = 0;

    while( ( ( max_iterations == -1 ) || ( iteration_count < max_iterations ) ) && 
           next) {

      next=false;

      println("${next} Finding all entries from ${collection} where lastModified > ${monitor_info.maxts}");
      def batch

      System.println("Process all items since ts:${monitor_info.maxts}");
      batch = db."${collection}".find( [ lastModified : [ $gt : monitor_info.maxts ] ] ).sort(lastModified:1).limit(batch_size+1);

      println("Query completed, batchsize = ${batch.size()}");

      int counter = 0;

      batch.each { r ->
        if ( counter < batch_size ) {
          counter++;
          processing_closure.call(r)
          monitor_info.maxid = r._id;
          monitor_info.maxts = r.lastModified;
          println("* ${iteration_count}/${counter}/${batch_size} : ${monitor_info.maxts}, ${monitor_info.maxid}");
        }
        else {
          // We've reached record batch_size+1, which means there is at least 1 more record to process. We should loop,
          // assuming we haven't passed max_iterations
          println("Counter has reached ${batch_size+1}, reset maxid");
          println("First record of next batch should be ${r._id}");
          next=true
        }
      }
      println("Saving monitor info ${monitor_info}");
      db.monitors.save(monitor_info);
      iteration_count++;
    }

    println("Complete");
  }
}
