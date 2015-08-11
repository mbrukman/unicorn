package unicorn.json

import java.nio.charset.Charset
import java.nio.{CharBuffer, ByteBuffer}

/**
 * Created by lihb on 8/11/15.
 */
trait JsonSerializerHelper {
  /** End of document */
  val END_OF_DOCUMENT             : Byte = 0x00

  /** End of string */
  val END_OF_STRING               : Byte = 0x00

  /** Type markers */
  val TYPE_DOUBLE                 : Byte = 0x01
  val TYPE_STRING                 : Byte = 0x02
  val TYPE_DOCUMENT               : Byte = 0x03
  val TYPE_ARRAY                  : Byte = 0x04
  val TYPE_BINARY                 : Byte = 0x05
  val TYPE_UNDEFINED              : Byte = 0x06
  val TYPE_OBJECTID               : Byte = 0x07
  val TYPE_BOOLEAN                : Byte = 0x08
  val TYPE_DATETIME               : Byte = 0x09
  val TYPE_NULL                   : Byte = 0x0A
  val TYPE_REGEX                  : Byte = 0x0B
  val TYPE_DBPOINTER              : Byte = 0x0C
  val TYPE_JAVASCRIPT             : Byte = 0x0D
  val TYPE_SYMBOL                 : Byte = 0x0E
  val TYPE_JAVASCRIPT_WITH_SCOPE  : Byte = 0x0F
  val TYPE_INT32                  : Byte = 0x10
  val TYPE_TIMESTAMP              : Byte = 0x11
  val TYPE_INT64                  : Byte = 0x12
  val TYPE_MINKEY                 : Byte = 0xFF.toByte
  val TYPE_MAXKEY                 : Byte = 0x7F

  /** Binary subtypes */
  val BINARY_SUBTYPE_GENERIC      : Byte = 0x00
  val BINARY_SUBTYPE_FUNCTION     : Byte = 0x01
  val BINARY_SUBTYPE_BINARY_OLD   : Byte = 0x02
  val BINARY_SUBTYPE_UUID         : Byte = 0x03
  val BINARY_SUBTYPE_MD5          : Byte = 0x05
  val BINARY_SUBTYPE_USER_DEFINED : Byte = 0x80.toByte

  val TRUE                        : Byte = 0x01
  val FALSE                       : Byte = 0x00

  // string encoder/decoder
  val charset = Charset.forName("UTF-8")

  /**
   * Helper function convert ByteBuffer to Array[Byte]
   */
  implicit def buffer2Bytes(buffer: ByteBuffer): Array[Byte] = {
    val bytes = new Array[Byte](buffer.position)
    buffer.position(0)
    buffer.get(bytes)
    bytes
  }

  def cstring(string: String)(implicit buffer: ByteBuffer): Unit = {
    val bytes = string.getBytes(charset)
    buffer.put(bytes)
    buffer.put(END_OF_STRING)
  }

  def serialize(json: JsBoolean, ename: Option[String])(implicit buffer: ByteBuffer): Unit = {
    buffer.put(TYPE_BOOLEAN)
    if (ename.isDefined) cstring(ename.get)
    buffer.put(if (json.value) TRUE else FALSE)
  }

  def serialize(json: JsInt, ename: Option[String])(implicit buffer: ByteBuffer): Unit = {
    buffer.put(TYPE_INT32)
    if (ename.isDefined) cstring(ename.get)
    buffer.putInt(json.value)
  }

  def serialize(json: JsLong, ename: Option[String])(implicit buffer: ByteBuffer): Unit = {
    buffer.put(TYPE_INT64)
    if (ename.isDefined) cstring(ename.get)
    buffer.putLong(json.value)
  }

  def serialize(json: JsDouble, ename: Option[String])(implicit buffer: ByteBuffer): Unit = {
    buffer.put(TYPE_DOUBLE)
    if (ename.isDefined) cstring(ename.get)
    buffer.putDouble(json.value)
  }

  def serialize(json: JsDate, ename: Option[String])(implicit buffer: ByteBuffer): Unit = {
    buffer.put(TYPE_DATETIME)
    if (ename.isDefined) cstring(ename.get)
    buffer.putLong(json.value.getTime)
  }

  def serialize(json: JsString, ename: Option[String])(implicit buffer: ByteBuffer): Unit = {
    buffer.put(TYPE_STRING)
    if (ename.isDefined) cstring(ename.get)
    val bytes = json.value.getBytes(charset)
    buffer.putInt(bytes.length)
    buffer.put(bytes)
  }

  def serialize(json: JsBinary, ename: Option[String])(implicit buffer: ByteBuffer): Unit = {
    buffer.put(TYPE_BINARY)
    if (ename.isDefined) cstring(ename.get)
    buffer.putInt(json.value.size)
    buffer.put(BINARY_SUBTYPE_GENERIC)
    buffer.put(json.value)
  }

  def cstring()(implicit buffer: ByteBuffer): String = {
    val str = new collection.mutable.ArrayBuffer[Byte](64)
    var b = buffer.get
    while (b != 0) {str += b; b = buffer.get}
    new String(str.toArray)
  }

  def ename()(implicit buffer: ByteBuffer): String = cstring

  def boolean()(implicit buffer: ByteBuffer): JsBoolean = {
    val b = buffer.get
    if (b == 0) JsFalse else JsTrue
  }

  def int()(implicit buffer: ByteBuffer): JsInt = {
    val x = buffer.getInt
    if (x == 0) JsInt.zero else JsInt(x)
  }

  def long()(implicit buffer: ByteBuffer): JsLong = {
    val x = buffer.getLong
    if (x == 0) JsLong.zero else JsLong(x)
  }

  def double()(implicit buffer: ByteBuffer): JsDouble = {
    val x = buffer.getDouble
    if (x == 0.0) JsDouble.zero else JsDouble(x)
  }

  def date()(implicit buffer: ByteBuffer): JsDate = {
    JsDate(buffer.getLong)
  }

  def string()(implicit buffer: ByteBuffer): JsString = {
    val length = buffer.getInt
    val dst = new Array[Byte](length)
    buffer.get(dst)
    JsString(new String(dst, charset))
  }

  def binary()(implicit buffer: ByteBuffer): JsBinary = {
    val length = buffer.getInt
    val subtype = buffer.get
    val dst = new Array[Byte](length)
    buffer.get(dst)
    JsBinary(dst)
  }
}