/* Distributed as part of prestasac, a Scala client for the PrestaShop Web Service
 *
 * Copyright (c) 2011 Alex Dean
 *
 * prestasac is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * prestasac is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with prestasac. If not, see <http://www.gnu.org/licenses/>.
 */
package co.orderly.prestasac

import _root_.java.net.URLEncoder

import org.apache.http.StatusLine
import org.apache.http.message._
import org.apache.http.auth._
import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.impl.client._

import scalaj.collection.Imports._

import scala.xml._

/**
 * Instantiate the PrestaShopWebService to start executing operations against the PrestaShop Web Service
 */
class PrestaShopWebService(
  var apiURL:  String,
  val apiKey:  String,
  val debug:   Boolean = true) {
  
  // Compatible versions of the PrestaShop Web Service
  val MIN_PRESTASHOP_VERSION = "1.4.0.17"
  val MAX_PRESTASHOP_VERSION = "1.4.2.3"

  // For URL encoding
  val UTF8_CHARSET = "UTF-8";

  // Append a trailing slash to the API URL if there isn't one
  if (!apiURL.matches(".*/")) apiURL += "/"

  /**
   * Check the response status and throw an exception if the server didn't return 200 or 201 code
   * @param status Status of the response
   * @return Status code
   */
  protected def check(status: StatusLine): Int = {

    val code = status.getStatusCode

    // Returns code if 200 or 201, otherwise throws an exception
    code match {
      case 200 => code
      case 201 => code
      case _   => throw new PrestaShopWebServiceException(
        "This call to the PrestaShop Web Service failed and returned an HTTP status of %d. That means: %s.".format(
          code, status.getReasonPhrase
        )
      )
    }
  }

  /**
   * Handles an HTTP request to PrestaShop Web Service. Uses Apache HttpClient
   * @param url Resource to request
   * @param args Parameters to configure the HTTP request
   * @return A tuple containing the response code, body and header
   */
  protected def execute(
    request:    HttpUriRequest,
    xml:        Option[Elem],
    noBody:     Boolean = false): Tuple3[Int, String, InputStream] = {

    // Debug show the URL we're getting
    if (debug) Console.println("URL to request: " + request.getURI)

    // Create the Http Client and attach authentication
    val httpClient = new DefaultHttpClient
    httpClient.getCredentialsProvider().setCredentials(
      new AuthScope(request.getURI.getHost, request.getURI.getPort),
      new UsernamePasswordCredentials(apiKey, "")
    )



    // TODO
    // Set no body flag

    // TODO
    // Pass in XML



    // Get the response, status code, body and headers
    val response = httpClient.execute(request)
    val code = check(response.getStatusLine())
    val header = response.getAllHeaders().mkString("\n")
    val data = response.getEntity().getContent()

    return (code, header, body) // Return salient data in a tuple
  }

  /**
   * Loads an XML into an Elem from a String
   * Throws an exception if there is no XML or it won't validate
   * @param xml The XML string to parse
   * @return The parsed XML in an Elem ready to work with
   */
  protected def parse(xml: String): Elem = {

    if (xml.isEmpty) {
      throw new PrestaShopWebServiceException("HTTP XML response is empty")
    } else {
      try {
        val xml = XML.loadString(xml)
        if (debug) {
          val ppr = new PrettyPrinter(80,2)
          Console.println("Returned XML: \n" + ppr.format(xml))
        }
      } catch {
        case e => {
          if (debug) Console.println("Unparseable XML: \n" + xml)
          e.printStackTrace()
          throw new PrestaShopWebServiceException("HTTP XML response is not parsable")
        }
      }
    }
    return xml
  }

  /**
   * Validates that the parameters are all either 'filter', 'display', 'sort' or 'limit'
   * Throws a PrestaShopWebServiceException if not
   * @param params Parameters to validate
   * @return The original parameters if everything is okay
   */
  protected def validate(params: Map[String, String]): Map[String, String] = {

    params.map(
      (param) => if (!(List("filter", "display", "sort", "limit") contains param._1) )
        throw new PrestaShopWebServiceException("Parameter %s is not supported".format(param._1))
    )
    return params
  }

  /**
   * Returns a canonicalized, escaped string of &key=value pairs from a Map of parameters
   * @param params A map of parameters ('filter', 'display' etc)
   * @return A canonicalized escaped string of the parameters
   */
  protected def canonicalize(params: Map[String, String]): String = {

    val nameValues = params.map { param => new BasicNameValuePair(param._1, param._2) }
    URLEncodedUtils.format(nameValues.toSeq.asJava, UTF8_CHARSET)
  }

  /**
   * Add (POST) a resource, self-assembly version
   * @param resource Type of resource to add
   * @param xml Full XML of new resource
   * @return XML response from Web Service
   */
  def add(resource: String, xml: Elem): Elem = {
    addURL(apiURL + resource, xml)
  }

  /**
   * Add (POST) a resource, URL version
   * @param url Full URL for a POST request to the Web Service
   * @param xml Full XML of new resource
   * @return XML response from Web Service
   */
  def addURL(url: String, xml: Elem): Elem = {
    parse(execute(new HttpPost(url), Some(xml))._2) // Execute the API call, parse the body (2nd item in tuple) and return the parsed XML
  }

  /**
   * Retrieve (GET) a resource, self-assembly version without parameters
   * @param resource Type of resource to retrieve
   * @param id Resource ID to retrieve
   * @return XML response from Web Service
   */
  // TODO: add support for optional ID
  def get(resource: String, id: Int): Elem = {
    get(resource, id, None)
  }

  /**
   * Retrieve (GET) a resource, self-assembly version with parameters
   * @param resource Type of resource to retrieve
   * @param id Resource ID to retrieve
   * @param params Map of parameters (one or more of 'filter', 'display', 'sort', 'limit')
   * @return XML response from Web Service
   */
  // TODO: add support for optional ID
  def get(resource: String, id: Int, params: Map[String, String]): Elem = {
    get(resource, id, Some(params))
  }

  /**
   * Retrieve (GET) a resource, helper version using Options
   * @param resource Type of resource to retrieve
   * @param id Resource ID to retrieve
   * @param params Optional Map of parameters (one or more of 'filter', 'display', 'sort', 'limit')
   * @return XML response from Web Service
   */
  // TODO: add support for optional ID
  protected def get(resource: String, id: Int, params: Option[Map[String, String]]): Elem = {
    getURL(
      apiURL + resource + "/" + id + "?" +
      (if (params.isDefined) canonicalize(validate(params.get)) else "")
    )
  }

  /**
   * Retrieve (GET) a resource, URL version
   * @param url A URL which explicitly sets the resource type and ID to retrieve
   * @return XML response from the Web Service
   */
  def getURL(url: String): Elem = {
    parse(execute(new HttpGet(url), None)._3) // Execute the API call, parse the body (3rd item in tuple) and return the parsed XML
  }

  /**
   * Head (HEAD) an individual resource or all resources of a type, self-assembly version
   * @param resource Type of resource to head
   * @return Header from Web Service's response
   */
  def head(resource: String): String = {
    head(resource, None, None)
  }

  /**
   * Head (HEAD) an individual resource or all resources of a type, self-assembly version
   * @param resource Type of resource to head
   * @param id Resource ID to head (if not provided, head all resources of this type)
   * @return Header from Web Service's response
   */
  def head(resource: String, id: Int): String = {
    head(resource, Some(id), None)
  }

  /**
   * Head (HEAD) an individual resource or all resources of a type, self-assembly version
   * @param resource Type of resource to head
   * @param id Resource ID to head (if not provided, head all resources of this type)
   * @param params Map of parameters (one or more of 'filter', 'display', 'sort', 'limit')
   * @return Header from Web Service's response
   */
  def head(resource: String, id: Int, params: Map[String, String]): String = {
    head(resource, Some(id), Some(params))
  }

  /**
   * Head (HEAD) an individual resource or all resources of a type, helper version using Options
   * @param resource Type of resource to head
   * @param id Optional resource ID to head (if not provided, head all resources of this type)
   * @param params Optional Map of parameters (one or more of 'filter', 'display', 'sort', 'limit')
   * @return Header from Web Service's response
   */
  protected def head(resource: String, id: Option[Int] = None, params: Option[Map[String, String]]): String = {
    headURL(
      apiURL + resource +
      (if (id.isDefined) "/" + id.get else "") + "?" +
      (if (params.isDefined) canonicalize(validate(params.get)) else "")
    )
  }

  /**
   * Head (HEAD) an individual resource or all resources of a type, URL version
   * @param url Full URL for the HEAD request to the Web Service
   * @return Header from Web Service's response
   */
  def headURL(url: String): String = {
    execute(new HttpHead(url), None, noBody = true)._2 // Return the header (2nd item in execute's return tuple)
  }

  /**
   * Edit (PUT) a resource, self-assembly version
   * @param resource Type of resource to update
   * @param id Resource ID to update
   * @param xml Modified XML of the resource
   * @return XML response from Web Service
   */
  def edit(resource: String, id: Int, xml: Elem): Elem = {
    editURL(apiURL + resource + "/" + id, xml)
  }

  /**
   * Edit (PUT) a resource, URL version
   * @param url A URL which explicitly sets the resource type and ID to edit
   * @param xml Modified XML of the resource
   * @return XML response from Web Service
   */
  def editURL(url: String, xml: Elem): Elem = {
    parse(execute(new HttpPut(url), Some(xml))._3) // Execute the API call, parse the body (3rd item in tuple) and return the parsed XML
  }

  /**
   * Delete (DELETE) a resource, self-assembly version supporting one ID
   * This version takes a resource type and an array of IDs to delete
   * @param resource The type of resource to delete (e.g. "orders")
   * @param id An ID of this resource type, to delete
   */
  def delete(resource: String, id: Int) {
    deleteURL(apiURL + resource + "/" + id)
  }

  /**
   * Delete (DELETE) a resource, self-assembly version supporting multiple IDs
   * This version takes a resource type and an array of IDs to delete
   * @param resource The type of resource to delete (e.g. "orders")
   * @param ids An array of IDs of this resource type, to delete
   */
  def delete(resource: String, ids: Array[Int]) {
    deleteURL(apiURL + resource + "/?id=[%s]".format(ids.mkString(",")))
  }

  /**
   * Delete (DELETE) a resource, URL version
   * @param url A URL which explicitly sets resource type and resource ID
   */
  def deleteURL(url: String) {
    execute(new HttpDelete(url), None)
  }
}

/**
 * Custom runtime exception for this library
 */
class PrestaShopWebServiceException(message: String) extends RuntimeException(message) {
}