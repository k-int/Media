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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
  
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
  
def starttime = System.currentTimeMillis();
def testuri = "http://media.culturegrid.org.uk/mediaLibrary/NCMG/BLDIDHH002634.jpg"

println("Startup.. Initialising at ${starttime}");

// loadRemoteImage(testuri);

println("Add image history");

File f = new File('images/file');

def canonical_identifier = java.util.UUID.randomUUID().toString()

def st = new StegTest()

// Hide the canonical identifier in the image
def new_file = st.encode(f,canonical_identifier);

println("hidden identifier ${canonical_identifier} in ${new_file}");

// Add the image metadata to the newly generated file
addImageHistoryTag(new_file, canonical_identifier);

println("Test decode - looking for steghide contents in ${new_file}");
println("Result of encoding: ${st.decode(new_file)}");

println("Completed after ${System.currentTimeMillis() - starttime}ms");


def loadRemoteImage(uri) {

  File image_dir = new File('images')
  if ( !image_dir.exists() ) {
    image_dir.mkdirs();
  }

  File output_file = new File('images/file');
  def out = new BufferedOutputStream(new FileOutputStream(output_file));

  out << new URL(testuri).openStream() 

  out.close()
}

/**
 * Example of adding an EXIF item to metadata, in this case using ImageHistory field. 
 * (I have no idea if this is an appropriate use of ImageHistory, or not, just picked
 * a field to update that looked like it wasn't commonly mucked with.)
 * @param file
*/
def addImageHistoryTag(File file, identifier) {

  println("Adding image history info to ${file}");

  File dst = null;
  IImageMetadata metadata = null;
  JpegImageMetadata jpegMetadata = null;
  TiffImageMetadata exif = null;
  OutputStream os = null;
  TiffOutputSet outputSet = new TiffOutputSet();
  
  // establish metadata
  try {
      metadata = Sanselan.getMetadata(file);
  } catch (ImageReadException e) {
      e.printStackTrace();
  } catch (IOException e) {
      e.printStackTrace();
  }
  
  // establish jpegMedatadata
  if (metadata != null) {
      jpegMetadata = (JpegImageMetadata) metadata;
  }
  
  // establish exif
  if (jpegMetadata != null) {
      exif = jpegMetadata.getExif();
  }
  
  // establish outputSet
  if (exif != null) {
      try {
          outputSet = exif.getOutputSet();
      } catch (ImageWriteException e) {
          e.printStackTrace();
      }
  }
  
  if (outputSet != null) {         
      // check if field already EXISTS - if so remove         

      // add field 
      try {  

          String fieldData = "This is the image history tag value......"; 

          TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
          addToDirectory(outputSet, TiffConstants.EXIF_TAG_XPCOMMENT,"MEDIA This is the image history tag value......", exifDirectory);
          addToDirectory(outputSet, TiffConstants.EXIF_TAG_COPYRIGHT,"MEDIA copyright statement", exifDirectory);
          addToDirectory(outputSet, TiffConstants.EXIF_TAG_IMAGE_ID ,"MEDIA image id ${identifier}", exifDirectory);
          addToDirectory(outputSet, TiffConstants.EXIF_TAG_OWNER_NAME,"MEDIA Owner Name", exifDirectory);
          addToDirectory(outputSet, TiffConstants.EXIF_TAG_SERIAL_NUMBER,"MEDIA Serial Number", exifDirectory);
          // addToDirectory(outputSet, TiffConstants.EXIF_TAG_USER_COMMENT,"MEDIA User Comment", exifDirectory);
          addToDirectory(outputSet, TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION,"MEDIA Image Description", exifDirectory);
          addToDirectory(outputSet, TiffConstants.EXIF_TAG_ARTIST,"MEDIA Artist == Creator", exifDirectory);
          addToDirectory(outputSet, new TagInfo('CustomTag', 0xff11, TiffFieldTypeConstants.FIELD_TYPE_ASCII),"A Media Custom Tag... Flooflip", exifDirectory);

      } catch (ImageWriteException e) {
  
          e.printStackTrace();
      }
  }
   
  try {
      dst = new File("Hallo.jpg");
      os = new FileOutputStream(dst);
      os = new BufferedOutputStream(os);
  } catch (IOException e) {
      e.printStackTrace();
  }
  
  // write/update EXIF metadata to output stream
  try {
      new ExifRewriter().updateExifMetadataLossless(file, os, outputSet);
  } catch (ImageReadException e) {
      e.printStackTrace();
  } catch (ImageWriteException e) {
      e.printStackTrace();
  } catch (IOException e) {
      e.printStackTrace();
  } finally {
    if (os != null) {
      try {
        os.close();
      } 
      catch (IOException e) {
      }
    }
  }
}
   
def addToDirectory(outputSet, tag, data, directory) {

  // Remove any existing tag
  TiffOutputField existing_tag = outputSet.findField(tag);
  if (existing_tag != null) {
    System.out.println("REMOVE ${tag}");
    outputSet.removeField(tag);
  }                    
  TiffOutputField newField = new TiffOutputField(
                    tag,
                    // TiffFieldTypeConstants.FIELD_TYPE_BYTE,
                    TiffFieldTypeConstants.FIELD_TYPE_ASCII,
                    data.length(),
                    data.getBytes());
  // TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
  // exifDirectory.add(imageHistory);
  directory.add(newField);
}
