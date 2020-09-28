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

import java.io.{BufferedReader, UnsupportedEncodingException}
import java.security.Principal
import java.util
import java.util.Locale

import com.scientiamobile.wurfl.wmclient.Model
import javax.servlet.{RequestDispatcher, ServletInputStream}
import javax.servlet.http.{Cookie, HttpServletRequest, HttpSession}
import org.apache.commons.collections.MapUtils
import org.apache.commons.collections.iterators.{EmptyIterator, IteratorEnumeration}
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import org.testng.annotations.{BeforeClass, Test}

import scala.collection.immutable.HashMap
import scala.collection.mutable

@Test
class WmClientTest extends AnyFlatSpec with Matchers {

  import com.scientiamobile.wurfl.wmclient.WmException
  import org.apache.commons.lang.StringUtils

  var _client: WmClient = null

  @BeforeClass
  @throws[WmException]
  def createTestClient(): WmClient = {
    var host = "localhost"
    var port = "8080"
    val envHost = System.getenv("WM_HOST")
    val envPort = System.getenv("WM_PORT")
    if (StringUtils.isNotEmpty(envHost)) {
      host = envHost
    }

    if (StringUtils.isNotEmpty(envPort)) {
      port = envPort
    }
    _client = WmClient.apply("http", host, port, "")
    _client
  }


  @throws[WmException]
  def createCachedTestClient(cacheSize: Int): WmClient = {
    var host = "localhost"
    var port = "8080"
    val envHost = System.getenv("WM_HOST")
    val envPort = System.getenv("WM_PORT")
    if (StringUtils.isNotEmpty(envHost)) {
      host = envHost
    }
    if (StringUtils.isNotEmpty(envPort)) {
      port = envPort
    }
    val cl = WmClient.apply("http", host, port, "")
    cl.setCacheSize(cacheSize)
    cl
  }

  it should "successfully create a WURFL microservice client" in {
    _client = createTestClient()
    assert(_client != null)
    assert(_client.getImportantHeaders().length > 0)
    assert(_client.getVirtualCaps.length > 0)
    assert(_client.getStaticCaps.length > 0)
    _client.destroyConnection()
  }
  it should "throw Wm exception if WM server is not configured correctly" in {
    assertThrows[WmException] {
      WmClient.apply("http", "localhost", "18080", "")
    }
  }

  it should "throw Wm exception if WM server host parameter is missing" in {
    assertThrows[WmException] {
      WmClient.apply("http", "", "8080", "")
    }
  }

  it should "throw Wm exception if all server connection values are missing" in {
    assertThrows[WmException] {
      WmClient.apply("", "", "", "")
    }
  }

  it should "retrieve WM server info" in {
    _client = createTestClient()
    val jsonInfoData = _client.getInfo()
    assert(jsonInfoData != null)
    assert(jsonInfoData.getWurflInfo.length > 0)
    assert(jsonInfoData.getStaticCaps.length > 0)
    assert(jsonInfoData.getVirtualCaps.length > 0)
    _client.destroyConnection()
  }

  it should "perform a device detection using a User-Agent as input" in {
    _client = createTestClient()
    val ua = "Mozilla/5.0 (Linux; Android 7.0; SAMSUNG SM-G950F Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/5.2 Chrome/51.0.2704.106 Mobile Safari/537.36"
    val device = _client.lookupUseragent(ua)
    assert(device != null)
    val capabilities = device.capabilities
    val dcount = capabilities.size
    assert(dcount >= 40)

    assert("SM-G950F" == capabilities.get("model_name"))
    assert("true" == capabilities.get("is_smartphone"))
    assert("false" == capabilities.get("is_smarttv"))
    _client.destroyConnection()

  }
  it should "perform a device detection using a User-Agent as input requesting a specific set of capabilities" in {
    val reqCaps = Array("brand_name", "model_name", "physical_screen_width", "device_os", "is_android", "is_ios", "is_app")
    _client = createTestClient()
    _client.setRequestedCapabilities(reqCaps)
    val ua = "Mozilla/5.0 (Nintendo Switch; WebApplet) AppleWebKit/601.6 (KHTML, like Gecko) NF/4.0.0.5.9 NintendoBrowser/5.1.0.13341"
    val device = _client.lookupUseragent(ua)
    assert(device != null)
    val capabilities = device.capabilities
    assert(capabilities != null)
    assert("Nintendo" == capabilities.get("brand_name"))
    assert("Switch" == capabilities.get("model_name"))
    assert("false" == capabilities.get("is_android"))
    assert(8 == capabilities.size)
    _client.destroyConnection()
  }

  it should "return a generic device when an empty user-agent is passed" in {
    _client = createTestClient()
    try {
      val device = _client.lookupUseragent("")
      assert(device != null)
      assert(device.capabilities.get("wurfl_id") == "generic")
    } catch {
      case e: WmException =>
        fail(e.getMessage)
    }
    finally {
      _client.destroyConnection()
    }
  }

  it should "return a generic device when a null user-agent is passed" in {
    _client = createTestClient()
    try {
      val device = _client.lookupUseragent(null)
      assert(device != null)
      assert(device.capabilities.get("wurfl_id") == "generic")
    } catch {
      case e: WmException =>
        fail(e.getMessage)
    }
    finally {
      _client.destroyConnection()
    }
  }

  it should "perform a device detection using a wurfl_id as input" in {
    _client = createTestClient()
    val device = _client.lookupDeviceId("nokia_generic_series40")
    assert(device != null)
    val capabilities = device.capabilities
    assert(capabilities != null)
    // num caps + num vcaps + wurfl id
    assert(capabilities.size >= 40)
    assert("false" == capabilities.get("is_android"))
    assert("128" == capabilities.get("resolution_width"))
    _client.destroyConnection()
  }

  it should "perform a device detection using a wurfl_id as input and a list of requested capabilities as a filter" in {
    _client = createTestClient()
    val reqCaps = Array("brand_name", "is_smarttv")
    val reqvCaps = Array("is_app", "is_app_webview")
    _client.setRequestedStaticCapabilities(reqCaps)
    _client.setRequestedVirtualCapabilities(reqvCaps)
    val device = _client.lookupDeviceId("generic_opera_mini_version1")
    assert(device != null)
    val capabilities = device.capabilities
    assert(capabilities != null)
    assert("Opera" == capabilities.get("brand_name"))
    assert("false" == capabilities.get("is_smarttv"))
    assert(5 == capabilities.size)
    _client.destroyConnection()
  }

  it should "return an exception if a non existing wurfl_id is passed to lookupDeviceId " in {
    _client = createTestClient()
    var exc: Boolean = false
    try _client.lookupDeviceId("nokia_generic_series40_wrong")
    catch {
      case e: WmException =>
        exc = true
        assert(e.getMessage.contains("device is missing"))
    }
    finally {
      _client.destroyConnection()
    }
  }

    it should "return an exception if a null wurfl_id is passed to lookupDeviceId " in {
      _client = createTestClient()
      var exc: Boolean = false
      try _client.lookupDeviceId(null)
      catch {
        case e: WmException =>
          exc = true
          assert(e.getMessage.contains("device is missing"))
      }
      finally {
        _client.destroyConnection()
      }
    }

  it should "perform a device detection using a HttpServletRequest object as input " in {

    _client = createTestClient()
    val request = createTestRequest(true)
    val device = _client.lookupRequest(request)
    assert(device!= null)
    val capabilities = device.capabilities
    assert(capabilities != null)
    assert(capabilities.size >= 40)
    assert("Smart-TV" == capabilities.get("form_factor"))
    assert("5.1.0.13341" == capabilities.get("advertised_browser_version"))
    assert("false" == capabilities.get("is_app"))
    assert("false" == capabilities.get("is_app_webview"))
    assert("Nintendo" == capabilities.get("advertised_device_os"))
    assert("Nintendo Switch" == capabilities.get("complete_device_name"))
    assert("nintendo_switch_ver1" == capabilities.get("wurfl_id"))
    _client.destroyConnection()
  }

  it should "perform a device detection using a HttpServletRequest object as input and a filter with a set of required capabilities" in {
    _client = createTestClient()
    val reqCaps = Array("is_mobile", "form_factor", "is_app", "complete_device_name", "advertised_device_os", "brand_name")
    _client.setRequestedCapabilities(reqCaps)
    val device = _client.lookupRequest(createTestRequest(true))
    val capabilities = device.capabilities
    assert(capabilities != null)
    assert(7 == capabilities.size)
    assert("false" == capabilities.get("is_app"))
    assert("Nintendo" == capabilities.get("advertised_device_os"))
    assert("Nintendo Switch" == capabilities.get("complete_device_name"))
    assert("nintendo_switch_ver1" == capabilities.get("wurfl_id"))
    _client.destroyConnection()
  }

  it should "perform a device detection using a header map as input" in {
    _client = createTestClient()
    val headers = new util.HashMap[String,String]()
    headers.put("User-Agent".toLowerCase, "Mozilla/5.0 (Nintendo Switch; WebApplet) AppleWebKit/601.6 (KHTML, like Gecko) NF/4.0.0.5.9 NintendoBrowser/5.1.0.13341")
    headers.put("Content-Type".toLowerCase, "gzip, deflate")
    headers.put("Accept-Encoding".toLowerCase, "application/json")
    headers.put("X-UCBrowser-Device-UA".toLowerCase, "Mozilla/5.0 (Nintendo Switch; ShareApplet) AppleWebKit/601.6 (KHTML, like Gecko) NF/4.0.0.5.9 NintendoBrowser/5.1.0.13341")
    headers.put("Device-Stock-UA".toLowerCase, "Mozilla/5.0 (Nintendo Switch; WifiWebAuthApplet) AppleWebKit/601.6 (KHTML, like Gecko) NF/4.0.0.5.9 NintendoBrowser/5.1.0.13341")
    val device = _client.lookupHeaders(headers)
    assert(device != null)
    val capabilities = device.capabilities
    assert(capabilities != null)
    assert(capabilities.size >= 40)
    assert("Smart-TV" == capabilities.get("form_factor"))
    assert("5.1.0.13341" == capabilities.get("advertised_browser_version"))
    assert("false" == capabilities.get("is_app"))
    assert("false" == capabilities.get("is_app_webview"))
    assert("Nintendo" == capabilities.get("advertised_device_os"))
    assert("Nintendo Switch" == capabilities.get("complete_device_name"))
    assert("nintendo_switch_ver1" == capabilities.get("wurfl_id"))
    _client.destroyConnection()
  }

  it should "return a generic device if a null or empty header map is provided " in {
    _client = createTestClient()
    var device = _client.lookupHeaders(null)
    assert(device != null)
    var capabilities = device.capabilities
    assert(capabilities != null)
    assert("generic" == capabilities.get("wurfl_id"))

    device = _client.lookupHeaders(new util.HashMap[String,String]())
    assert(device != null)
    capabilities = device.capabilities
    assert(capabilities != null)
    assert("generic" == capabilities.get("wurfl_id"))
    _client.destroyConnection()
  }

  it should "return a generic device when a http request without headers is provided and a capability filter is used" in {
    _client = createTestClient()
    try {
      val reqCaps = Array("brand_name", "is_wireless_device", "pointing_method", "model_name")
      _client.setRequestedCapabilities(reqCaps)
      // Create request to pass
      val device = _client.lookupRequest(createTestRequest(false))
      assert(device != null)
      assert(device.capabilities.get("wurfl_id") == "generic")
    } catch {
      case e: WmException =>
        fail(e.getMessage)
    } finally _client.setRequestedCapabilities(null)
  }

  it should "throw an exception when a null request is passed as input " in {
    _client = createTestClient()
    val caught =
      intercept[WmException] {
        _client.lookupRequest(null)
      }
    assert(caught.getMessage.contains("HttpServletRequest cannot be null") != null)
    _client.destroyConnection()
  }


  it should "return true if has a static capability, false otherwise" in {
    _client = createTestClient()
    assert(_client.hasStaticCapability("brand_name"))
    assert(_client.hasStaticCapability("model_name"))
    assert(_client.hasStaticCapability("is_smarttv"))
    // this is a virtual capability, so it shouldn't be returned
    assert(!_client.hasStaticCapability("is_app"))
    _client.destroyConnection()
  }

  it should "return true if has a virtual capability, false otherwise" in {
    _client = createTestClient()
    assert(_client.hasVirtualCapability("is_app"))
    assert(_client.hasVirtualCapability("is_smartphone"))
    assert(_client.hasVirtualCapability("form_factor"))
    assert(_client.hasVirtualCapability("is_app_webview"))
    // this is a static capability, so it shouldn't be returned
    assert(!_client.hasVirtualCapability("brand_name"))
    assert(!_client.hasVirtualCapability("is_wireless_device"))
    _client.destroyConnection()
  }

  it should "set the requested static and/or virtual capabilities to be downloaded on each request " in {
    _client = createTestClient()
    _client.setRequestedStaticCapabilities(Array[String]("wrong1", "brand_name", "is_ios"))
    _client.setRequestedVirtualCapabilities(Array[String]("wrong2", "brand_name", "is_ios"))

    val ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_2_1 like Mac OS X) AppleWebKit/602.4.6 (KHTML, like Gecko) Version/10.0 Mobile/14D27 Safari/602.1"
    var d = _client.lookupUseragent(ua)
    assert(d != null)
    assert(d.capabilities.size == 3)
    assert(d.capabilities.get("wrong1") == null)

    // This will reset static caps
    _client.setRequestedStaticCapabilities(null)
    d = _client.lookupUseragent(ua)
    assert(d.capabilities.size == 2)
    // If all required caps arrays are reset, ALL caps are returned
    _client.setRequestedVirtualCapabilities(null)
    d = _client.lookupUseragent(ua)
    val capsize = d.capabilities.size
    assert(capsize >= 40)
    _client.destroyConnection()
  }

  it should "download all device makers" in {
    _client = createTestClient()
    val makes = _client.getAllDeviceMakes()
    assert(makes != null)
    assert(makes.length > 2000)
  }

  it should "get all models for the given input maker name" in {
      val modelMktNames = _client.getAllDevicesForMake("Nokia")
      assert(modelMktNames != null)
      assert(modelMktNames.length > 700)
      assert(modelMktNames(0).modelName != null)
      assert(modelMktNames(5).marketingName != null)

      for (md <- modelMktNames) {
        assert(md != null)
      }
    }

  it should "download al device OSes" in {
    val oses = _client.getAllOSes()
    assert(oses != null)
    assert(oses.length >= 30)
  }

  it should "return all version numbers for a given device OS name " in {
    val osVersions = _client.getAllVersionsForOS("Android")
    assert(osVersions != null)
    assert(osVersions.length > 30)
    assert(osVersions(0) != null )
    _client.destroyConnection()
  }


  def createTestRequest(provideHeaders: Boolean): HttpServletRequest = {

    new HttpServletRequest() {
      private val headers = new java.util.HashMap[String,String]
      private val ua = "Mozilla/5.0 (Nintendo Switch; WebApplet) AppleWebKit/601.6 (KHTML, like Gecko) NF/4.0.0.5.9 NintendoBrowser/5.1.0.13341"
      private val xucbr = "Mozilla/5.0 (Nintendo Switch; ShareApplet) AppleWebKit/601.6 (KHTML, like Gecko) NF/4.0.0.5.9 NintendoBrowser/5.1.0.13341"
      private val dstkUa = "Mozilla/5.0 (Nintendo Switch; WifiWebAuthApplet) AppleWebKit/601.6 (KHTML, like Gecko) NF/4.0.0.5.9 NintendoBrowser/5.1.0.13341"

      override def getAuthType: String = null

      override def getCookies = new Array[Cookie](0)

      override def getDateHeader(s: String) = 0

      override def getHeader(key: String): String = {
        fillHeadersIfNeeded()
        headers.get(key.toLowerCase)
      }

      private def fillHeadersIfNeeded(): Unit = { // all headers are put lowercase to emulate real http servlet request behaviour
        if (MapUtils.isEmpty(headers) && provideHeaders) {
          headers.put("User-Agent".toLowerCase, ua)
          headers.put("Content-Type".toLowerCase, "gzip, deflate")
          headers.put("Accept-Encoding".toLowerCase, "application/json")
          headers.put("X-UCBrowser-Device-UA".toLowerCase, xucbr)
          headers.put("Device-Stock-UA".toLowerCase, dstkUa)
        }
      }

      override def getHeaders(s: String): IteratorEnumeration = {
        if (provideHeaders) {
          fillHeadersIfNeeded()
          return new IteratorEnumeration(headers.keySet.iterator)
        }
        new IteratorEnumeration(EmptyIterator.INSTANCE)
      }

      override def getHeaderNames: IteratorEnumeration = {
        fillHeadersIfNeeded()
        new IteratorEnumeration(headers.keySet.iterator)
      }

      override def getIntHeader(s: String) = 0

      override def getMethod: String = null

      override def getPathInfo: String = null

      override def getPathTranslated: String = null

      override def getContextPath: String = null

      override def getQueryString: String = null

      override def getRemoteUser: String = null

      override def isUserInRole(s: String) = false

      override def getUserPrincipal: Principal = null

      override def getRequestedSessionId: String = null

      override def getRequestURI: String = null

      override def getRequestURL: StringBuffer = null

      override def getServletPath: String = null

      override def getSession(b: Boolean): HttpSession = null

      override def getSession: HttpSession = null

      override def isRequestedSessionIdValid = false

      override def isRequestedSessionIdFromCookie = false

      override def isRequestedSessionIdFromURL = false

      override def isRequestedSessionIdFromUrl = false

      override def getAttribute(s: String): String = null

      override def getAttributeNames: java.util.Enumeration[_] = null

      override def getCharacterEncoding: String = null

      override def setCharacterEncoding(s: String): Unit = {}

      override def getContentLength = 0

      override def getContentType: String = null

      override def getInputStream: ServletInputStream = null

      override def getParameter(s: String): String = null

      override def getParameterNames: java.util.Enumeration[_] = null

      override def getParameterValues(s: String) = new Array[String](0)

      override def getParameterMap: java.util.Map[_, _] = null

      override def getProtocol: String = null

      override def getScheme: String = null

      override def getServerName: String = null

      override def getServerPort = 0

      override def getReader: BufferedReader = null

      override def getRemoteAddr: String = null

      override def getRemoteHost: String = null

      override def setAttribute(s: String, o: Any): Unit = { }

      override def removeAttribute(s: String): Unit = { }

      override def getLocale: Locale = null

      override def getLocales: java.util.Enumeration[_] = null

      override def isSecure = false

      override def getRequestDispatcher(s: String): RequestDispatcher = null

      override def getRealPath(s: String): String = null

      override def getRemotePort = 0

      override def getLocalName: String = null

      override def getLocalAddr: String = null

      override def getLocalPort = 0
    }
  }
}