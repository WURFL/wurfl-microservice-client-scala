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

  var _client:WmClient = null

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

}