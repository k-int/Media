package cgimp

import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import java.nio.charset.Charset


class RepositoryClientService {

  def uploadStream(document_bytes,target_service, data_provider) {

    log.debug("About to make post request");

    try {
      byte[] resource_to_deposit = document_bytes

      log.debug("Length of input stream is ${resource_to_deposit.length}");

      target_service.request(POST) {request ->
        requestContentType = 'multipart/form-data'

        // Much help taken from http://evgenyg.wordpress.com/2010/05/01/uploading-files-multipart-post-apache/
        def multipart_entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipart_entity.addPart( "owner", new StringBody( data_provider, "text/plain", Charset.forName( "UTF-8" )))  // Owner

        def uploaded_file_body_part = new org.apache.http.entity.mime.content.ByteArrayBody(resource_to_deposit, 'text/xml', 'filename')
        multipart_entity.addPart( "upload", uploaded_file_body_part)

        request.entity = multipart_entity;

        response.success = { resp, data ->
          log.debug("response status: ${resp.statusLine}")
          log.debug("Response data code: ${data?.code}");
        }

        response.failure = { resp ->
          log.error("Failure - ${resp}");
        }
      }
    }
    catch ( Exception e ) {
      log.error("Unexpected exception trying to read remote stream",e)
    }
    finally {
      log.debug("uploadStream try block completed");
    }
    log.debug("uploadStream completed");
  }
}
