package org.leveloneproject.central.kms.util

import scala.language.implicitConversions

object Bytes {
  implicit class ExtendedString(val value: String) {
    def fromHex: Array[Byte] = value.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)

    def fromUtf8: Array[Byte] = value.getBytes("UTF-8")
  }

  implicit class ExtendedBytArray(val buf: Array[Byte]) {
    def toHex: String = buf.map("%02X" format _).mkString
  }
}
