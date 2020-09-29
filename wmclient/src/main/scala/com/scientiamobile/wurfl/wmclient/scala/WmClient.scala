/*
Copyright 2020 ScientiaMobile Inc. http://www.scientiamobile.com
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.scientiamobile.wurfl.wmclient.scala

import java.util

import com.scientiamobile.wurfl.wmclient.{Model, WmException}
import javax.servlet.http.HttpServletRequest

/**
 * Class for Scala WM client. It is a wrapper for WURFL Microservice Java client to give Scala developers a more friendly
 * access to WURFL Microservice API.<br>
 *   WURFL Microservice Java client is mandatory to run the Scala one.
 * Author(s):  Andrea Castello
 * Date: 17/09/2020.
 */
class WmClient(private var wmjclient: com.scientiamobile.wurfl.wmclient.WmClient) {

  def lookupUseragent(userAgent: String): Model#JSONDeviceData = wmjclient.lookupUseragent(userAgent)

  /**
   * Performs a device detection using an HTTP request object, as passed from Java Web applications
   *
   * @param request an instance of HTTPServletRequest
   * @return An object containing the device capabilities
   * @throws WmException In case any error occurs during device detection
   */
  def lookupRequest(request: HttpServletRequest): Model#JSONDeviceData = wmjclient.lookupRequest(request)

  /**
   * @return A JSONInfoData instance holding the capabilities exposed from WM server, the headers used for device detection, WURFL file and API version
   * @throws WmException If server cannot send data or incomplete data are sent
   */
  def getInfo(): Model#JSONInfoData = wmjclient.getInfo

  /**
   * Returns the device matching the given WURFL ID
   *
   * @param deviceId a WURFL device identifier
   * @return An object containing the device capabilities
   * @throws WmException In case any error occurs
   */
  def lookupDeviceId(deviceId:String): Model#JSONDeviceData = wmjclient.lookupDeviceId(deviceId)

  /**
   * Performs a device detection using a map containing the http request headers.
   * Headers are handled as case insensitive (ie: header name "User-Agent" is equal to "UsEr-aGent")
   *
   * @param headers an instance of HTTPServletRequest
   * @return An object containing the device capabilities
   * @throws WmException In case any error occurs during device detection
   */
  def lookupHeaders (headers: util.Map[String, String]): Model#JSONDeviceData = wmjclient.lookupHeaders(headers)

  /**
   * @return getAllDeviceMakes returns a string array of all devices brand_name capabilities in WM server
   * @throws WmException In case a connection error occurs or malformed data are sent
   */
  def getAllDeviceMakes(): Array[String] = wmjclient.getAllDeviceMakes

  /**
   * @param make a brand name
   * @return An array of {@link com.scientiamobile.wurfl.wmclient.Model#JSONModelMktName} that contain values for model_name
   *         and marketing_name (the latter, if available).
   * @throws WmException In case a connection error occurs, malformed data are sent, or the given brand name parameter does not exist in WM server.
   */
  def getAllDevicesForMake(make: String): Array[Model#JSONModelMktName] = wmjclient.getAllDevicesForMake(make)

  /**
   * @return an array of all devices device_os capabilities in WM server
   * @throws WmException In case a connection error occurs or malformed data are sent
   */
  def getAllOSes(): Array[String] = wmjclient.getAllOSes

  /**
   * returns a slice
   *
   * @param osName a device OS name
   * @return an array containing device_os_version for the given os_name
   * @throws WmException In case a connection error occurs or malformed data are sent
   */
  def getAllVersionsForOS(osName: String): Array[String] = wmjclient.getAllVersionsForOS(osName)

  /**
   * Sets WURFL Microservice cache client size
   * @param size the desired cache size
   */
  def setCacheSize(size: Int) = wmjclient.setCacheSize(size)

  /**
   * Sets the list of static capabilities that WURFL microservice client will return with each device detection request
   * @param staticCaps list of desired static capabilities
   */
  def setRequestedStaticCapabilities(staticCaps: Array[String]) = wmjclient.setRequestedStaticCapabilities(staticCaps)

  /**
   * Sets the list of virtual capabilities that WURFL microservice client will return with each device detection request
   * @param vCaps list of desired virtual capabilities
   */
  def setRequestedVirtualCapabilities(vCaps: Array[String]) = wmjclient.setRequestedVirtualCapabilities(vCaps)

  /**
   * @param capName capability name
   * @return true if the given static capability is handled by this client, false otherwise
   */
  def hasStaticCapability(capName: String): Boolean = wmjclient.hasStaticCapability(capName)

  /**
   * @param vcapName capability name
   * @return true if the given virtual capability is handled by this client, false otherwise
   */
  def hasVirtualCapability(vcapName: String): Boolean = wmjclient.hasVirtualCapability(vcapName)

  def setRequestedCapabilities(capsList: Array[String]) = wmjclient.setRequestedCapabilities(capsList)

  /**
   * Deallocates all resources used by client. All subsequent usage of client will result in a WmException (you need to create the client again
   * with a call to WmClient.apply.
   *
   * @throws WmException In case of closing connection errors.
   */
  def destroyConnection() = wmjclient.destroyConnection

  /**
   * @return All static capabilities handled by this client
   */
  def getStaticCaps(): Array[String] = wmjclient.getStaticCaps

  /**
   * @return All the virtual capabilities handled by this client
   */
  def getVirtualCaps(): Array[String] = wmjclient.getVirtualCaps

  /**
   * @return list all HTTP headers used for device detection by this client
   */
  def getImportantHeaders(): Array[String] = wmjclient.getImportantHeaders

  /**
   * @return this WURFL Microservice client version
   */
  def getApiVersion(): String = wmjclient.getApiVersion

  /**
   * @return the sizes of the WURFL Microservice client cache. The returned array has two elements:
   *         the first one is the one used with headers/request/user-agent lookups,
   *         the second one is the one used with deviceID lookup.
   */
  def getActualCacheSizes(): Array[Int] = wmjclient.getActualCacheSizes

}

object WmClient {
  def apply(scheme: String, host: String, port: String, baseUri: String): WmClient =
    new WmClient(com.scientiamobile.wurfl.wmclient.WmClient.create(scheme, host, port, baseUri))
}