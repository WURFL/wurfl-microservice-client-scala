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

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import org.testng.annotations.{BeforeClass, Test}

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

  }

}