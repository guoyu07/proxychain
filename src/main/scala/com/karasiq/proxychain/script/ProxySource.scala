package com.karasiq.proxychain.script

import java.io.{IOException, InputStream}
import java.net.URL

import com.karasiq.fileutils.PathUtils._
import jdk.nashorn.internal.objects.NativeArray
import org.apache.commons.io.IOUtils

import scala.io.Source
import scala.util.control.Exception

/**
 * File/URL loader
 */
private[script] object ProxySource {
  // Default source encoding
  private def defaultEncoding: String = "UTF-8"

  /**
   * Converts InputStream to strings array
   * @param inputStream Source
   * @param encoding Encoding
   * @return JS strings array
   */
  private def asStringArray(inputStream: InputStream, encoding: String): NativeArray = {
    Exception.allCatch.andFinally(IOUtils.closeQuietly(inputStream)) {
      val source = Source.fromInputStream(inputStream, encoding)
      Conversions.asJsArray(source.getLines().filter(_.length > 0).toVector)
    }
  }

  private def urlAsInputStream(url: String): InputStream = {
    val connection = new URL(url).openConnection()
    connection.setConnectTimeout(10000)
    connection.setReadTimeout(10000)
    connection.getInputStream
  }

  private def fileAsInputStream(file: String): InputStream = {
    asPath(file).inputStream()
  }

  /**
   * Loads proxy list from URL
   */
  def fromURL(url: String, encoding: String): NativeArray = {
    Exception.catching(classOf[IOException])
      .either(urlAsInputStream(url)).fold(_ ⇒ Conversions.asJsArray(Nil), asStringArray(_, encoding))
  }

  /**
   * Loads proxy list from URL
   */
  def fromURL(url: String): NativeArray = fromURL(url, defaultEncoding)

  /**
   * Loads proxy list from file
   */
  def fromFile(file: String, encoding: String): NativeArray = {
    asStringArray(fileAsInputStream(file), encoding)
  }

  /**
   * Loads proxy list from URL
   */
  def fromFile(file: String): NativeArray = fromFile(file, defaultEncoding)
}
