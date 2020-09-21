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

object VersionUtils {

  /**
   *
   * @param ver1 version number
   * @param ver2 another version number
   * @return 0 when ver1 is the same of ver2, -1 when ver1 is older than ver2, 1 if ver2 is older than ver2
   * @throws IllegalArgumentException when ver1 and ver2 have different version patterns
   * @throws NullPointerException     when at least one of ver1,ver2 is null
   */
  def compareVersionNumbers(ver1: String, ver2: String): Int = {
    val ver1Toks = ver1.split("\\.")
    val ver2Toks = ver2.split("\\.")
    var tokensToCompare = ver1Toks.length
    if (ver2Toks.length < tokensToCompare) {
      tokensToCompare = ver2Toks.length
    }
    for (i <- 0 until tokensToCompare) {

      if(ver1Toks(i).toInt < ver2Toks(i).toInt){
          return -1
      } else if (ver1Toks(i).toInt < ver2Toks(i).toInt){
        return 1
      }
    }
    // No difference between strings return zero
    0
  }


}
