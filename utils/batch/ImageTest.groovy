#!/usr/bin/groovy

@GrabResolver(name='es', root='https://oss.sonatype.org/content/repositories/releases')
// @Grab(group='com.gmongo', module='gmongo', version='0.9.2')
@Grapes([
  @Grab(group='org.apache.httpcomponents', module='httpmime', version='4.1.2'),
  @Grab(group='com.gmongo', module='gmongo', version='0.9.2'),
  @Grab(group='org.apache.httpcomponents', module='httpclient', version='4.0'),
  @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.0'),
  @Grab(group='org.apache.sanselan', module='sanselan', version='0.97-incubator')
])

def starttime = System.currentTimeMillis();
def testuri = "http://media.culturegrid.org.uk/mediaLibrary/NCMG/BLDIDHH002634.jpg"

println("Startup.. Initialising at ${starttime}");

File image_dir = new File('images')
if ( !image_dir.exists() ) {
  image_dir.mkdirs();
}

File output_file = new File('images/file');
def out = new BufferedOutputStream(new FileOutputStream(output_file));

out << new URL(testuri).openStream() 

out.close()

println("Completed after ${System.currentTimeMillis() - starttime}ms");
