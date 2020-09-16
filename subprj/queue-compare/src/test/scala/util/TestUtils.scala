
package sni.util

object TestUtils {
  /**
   * Convert hexadecimal String to Array of Bytes.
   * @param hexStr Input Hexadecimal string.
   * @return Array of Bytes.
   */
  def hex2Bytes(hexStr: String): Array[Byte] =
    hexStr.replaceAll("[^0-9A-Fa-f]", "").
      sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
}
