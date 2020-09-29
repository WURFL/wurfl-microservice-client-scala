import com.scientiamobile.wurfl.wmclient.WmException
import com.scientiamobile.wurfl.wmclient.scala.WmClient

import scala.util.Sorting.quickSort

object Example {

  def main(args: Array[String]) {
    print("Running scala version ")
    println(scala.util.Properties.scalaPropOrElse("version.number", "unknown scala version"))

    try { // First we need to create a WM client instance, to connect to our WM server API at the specified host and port.
      val client = WmClient.apply("http", "localhost", "8080", "")
      // We ask Wm server API for some Wm server info such as server API version and info about WURFL API and file used by WM server.
      val info = client.getInfo()
      println("Printing WM server information")
      println("WURFL API version: " + info.getWurflApiVersion)
      println("WM server version:  " + info.getWmVersion)
      println("Wurfl file info: " + info.getWurflInfo)
      val ua = "Mozilla/5.0 (Linux; Android 7.1.1; ONEPLUS A5000 Build/NMF26X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Mobile Safari/537.36"
      // By setting the cache size we are also activating the caching option in WM client. In order to not use cache, you just to need to omit setCacheSize call
      client.setCacheSize(100000)
      // set the capabilities we want to receive from WM server
      client.setRequestedStaticCapabilities(Array[String]("brand_name", "model_name"))
      client.setRequestedVirtualCapabilities(Array[String]("is_smartphone", "form_factor"))
      println
      println("Detecting device for user-agent: " + ua)
      // Perform a device detection calling WM server API
      val device = client.lookupUseragent(ua)
      // Applicative error, ie: invalid input provided
      if (device.error != null && device.error.length > 0) println("An error occurred: " + device.error)
      else { // Let's get the device capabilities and print some of them
        val capabilities = device.capabilities
        println("Detected device WURFL ID: " + capabilities.get("wurfl_id"))
        println("Device brand & model: " + capabilities.get("brand_name") + " " + capabilities.get("model_name"))
        println("Detected device form factor: " + capabilities.get("form_factor"))
        if (capabilities.get("is_smartphone").equals("true")) println("This is a smartphone")
        // Iterate over all the device capabilities and print them
        println("All received capabilities")
        val it = capabilities.keySet.iterator
        while ( {
          it.hasNext
        }) {
          val k = it.next
          println(k + ": " + capabilities.get(k))
        }
      }
      // Get all the device manufacturers, and print the first twenty
      val limit = 20
      val deviceMakes = client.getAllDeviceMakes
      printf("Print the first %d Brand of %d retrieved from server\n", limit, deviceMakes.length)
      // Sort the device manufacturer names
      quickSort(deviceMakes)
      for (i <- 0 until limit) {
        printf(" - %s\n", deviceMakes(i))
      }
      // Now call the WM server to get all device model and marketing names produced by Apple
      println("Print all Model for the Apple Brand")
      val devNames = client.getAllDevicesForMake("Apple")
      // Sort ModelMktName objects by their model name
      devNames.sortWith(_.modelName >= _.modelName)

      for (modelMktName <- devNames) {
        printf(" - %s %s\n", modelMktName.modelName, modelMktName.marketingName)
      }
      // Now call the WM server to get all operative system names
      println("Print the list of OSes")
      val oses = client.getAllOSes
      // Sort and print all OS names
      quickSort(oses)
      for (os <- oses) {
        printf(" - %s\n", os)
      }
      // Let's call the WM server to get all version of the Android OS
      println("Print all versions for the Android OS")
      val osVersions = client.getAllVersionsForOS("Android")
      // Sort all Android version numbers and print them.
      quickSort(osVersions)
      for (ver <- osVersions) {
        printf(" - %s\n", ver)
      }
      // Cleans all client resources. Any call on client API methods after this one will throw a WmException
      client.destroyConnection
    } catch {
      case e: WmException =>
        // problems such as network errors  or internal server problems
        // note that WmException comes from the Java wrapped API
        println("An error has occurred: " + e.getMessage)
    }
  }
}
