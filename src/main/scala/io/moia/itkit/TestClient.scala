/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia
package itkit

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._

import scala.collection.immutable
import scala.concurrent.Future

/** Client specifications available in the test suite or individual test. */
trait TestClient {

  /** If known, the server uri to send the requests against. */
  def serverUri: Option[Uri]

  /** Exposes the http extension. This can be used in order to send requests. */
  def http: HttpExt

  /** Wraps the `singleRequest` of http extension for the sake of shorter notation.
    *
    * @param method method to be used in the request, e.g. HttpMethods.GET or HttpMethods.POST
    * @param path path to be used to send the request to.
    * @param headers optional headers to be included in the request.
    * @param entity optional entity to be attached to the request.
    * @param protocol protocol to be used for the request.
    * @return server response.
    */
  def request(
      method: HttpMethod,
      path: String,
      headers: immutable.Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`
  ): Future[HttpResponse] =
    http.singleRequest(
      request = HttpRequest(
        method = method,
        uri = serverUri.getOrElse(Uri./).withPath(Uri.Path(path)),
        headers = headers,
        entity = entity,
        protocol = protocol
      )
    )

  /** Wraps a GET `singleRequest` of http extension for the sake of shorter notation.
    *
    * @param path path to be used to send the request to.
    * @param headers optional headers to be included in the request.
    * @param entity optional entity to be attached to the request.
    * @param protocol protocol to be used for the request.
    * @return server response.
    */
  def getRequest(
      path: String,
      headers: immutable.Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`
  ): Future[HttpResponse] =
    request(HttpMethods.GET, path, headers, entity, protocol)

  /** Wraps a POST `singleRequest` of http extension for the sake of shorter notation.
    *
    * @param path path to be used to send the request to.
    * @param headers optional headers to be included in the request.
    * @param entity optional entity to be attached to the request.
    * @param protocol protocol to be used for the request.
    * @return server response.
    */
  def postRequest(
      path: String,
      headers: immutable.Seq[HttpHeader] = Nil,
      entity: RequestEntity = HttpEntity.Empty,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`
  ): Future[HttpResponse] =
    request(HttpMethods.POST, path, headers, entity, protocol)
}
