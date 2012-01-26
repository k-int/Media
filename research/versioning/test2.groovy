@Grab(group='net.sf.json-lib', module='json-lib', version='2.4', classifier='jdk15')
@Grab(group='commons-lang', module='commons-lang', version='2.4')

import org.apache.commons.lang.SystemUtils
import org.json.JSONObject


def printInfo() {
  if (SystemUtils.isJavaVersionAtLeast(5)) {
    println 'We are ready to use annotations in our Groovy code.'
  } else {
    println 'We cannot use annotations in our Groovy code.'
  }
}

printInfo()

