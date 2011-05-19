/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.clerezza.foafssl.auth

import javax.security.auth.Refreshable
import java.security.cert.X509Certificate
import org.slf4j.LoggerFactory
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.foafssl.ontologies.{RSA, CERT}
import java.util.Date
import org.apache.clerezza.rdf.core._
import java.math.BigInteger
import org.apache.clerezza.rdf.scala.utils.Preamble._

/**
 * Static methods for X509Claim. It makes it easier to read code when one knows which methods
 * have no need of any object state and which do. These methods could be moved to a library.
 */
object X509Claim {
  final val logger = LoggerFactory.getLogger(classOf[X509Claim])


  private def intValueOfHexString(s: String): BigInt = {
    val strval = cleanHex(s);
    BigInt(strval, 16);
  }


  /**
   * This takes any string and returns in order only those characters that are
   * part of a hex string
   *
   * @param strval
   *            any string
   * @return a pure hex string
   */

  private def cleanHex(strval: String) = {
    def legal(c: Char) = {
      //in order of appearance probability
      ((c >= '0') && (c <= '9')) ||
        ((c >= 'A') && (c <= 'F')) ||
        ((c >= 'a') && (c <= 'f'))
    }
    (for (c <- strval; if legal(c)) yield c)
  }

  /*private lazy val selectQuery = {
   val query = """PREFIX cert: <http://www.w3.org/ns/auth/cert#>
   PREFIX rsa: <http://www.w3.org/ns/auth/rsa#>
   SELECT ?m ?e ?mod ?exp
   WHERE {
   [] cert:identity ?webid ;
   rsa:modulus ?m ;
   rsa:public_exponent ?e .
   OPTIONAL { ?m cert:hex ?mod . }
   OPTIONAL { ?e cert:decimal ?exp . }
   }"""
   queryParser.parse(query).asInstanceOf[SelectQuery]
   }*/

  /**
   * gets the parts of key from rdf
   * @return (mod, exp)
   */
  def getPublicKeysInGraph(webId: UriRef, tc: TripleCollection): Array[(BigInt, BigInt)] = {
    import scala.collection.JavaConversions._
    val publicKeys = for (t <- tc.filter(null, CERT.identity, webId)) yield {
      t.getSubject
    }
    (for (p <- publicKeys) yield {
      val node = new GraphNode(p, tc)
      val modulusRes = node / RSA.modulus
      val modulus = intValueOfResource(modulusRes) match {
        case Some(x) => x
        case _ => BigInt(0)
      }
      val exponentRes = node / RSA.public_exponent
      val exponent = intValueOfResource(exponentRes) match {
        case Some(x) => x
        case _ => BigInt(0)
      }
      (modulus, exponent)
    }).toArray
  }

  /**
   * Extracts the URIs in the subject alternative name extension of an X.509
   * certificate
   *
   * @param cert X.509 certificate from which to extract the URIs.
   * @return Iterator of URIs as strings found in the subjectAltName extension.
   */
  def getClaimedWebIds(cert: X509Certificate): Iterator[String] = {
    //           throws CertificateParsingException {
    if (cert == null) {
      return Iterator.empty;
    }

    val names = cert.getSubjectAlternativeNames()
    if (names == null) {
      return Iterator.empty;
    }

    return new Iterator[String]() {
      val it = names.iterator
      var nxt: String= getNext()

      def hasNext(): Boolean =  nxt !=null;


      def next(): String = {
        val res = nxt
        nxt = getNext
        return res
      }

      def getNext(): String = {
        while (it.hasNext) {
          val altNme = it.next()
          val altTpe = altNme.get(0);
          val altObj = altNme.get(1);
          if ((altTpe.asInstanceOf[Int] == 6) && altObj.isInstanceOf[String]) {
            return altObj.asInstanceOf[String]
          }
        }
        return null
      }

    }

  }


  /**
   * @return the integer value if r is a typedLiteral of cert:hex or cert:decimal,
   * otherwise the integer value of the  cert:hex or cert:decimal property of r or
   * None if no such value available
   */
  def intValueOfResource(n: GraphNode): Option[BigInt] = {
    n ! match {
      case l: TypedLiteral => intValueOfTypedLiteral(l);
      case r: Resource => intValueOfResourceByProperty(n)
    }
  }

  /*
  	* This method helps us deal with the old style notation
  	* of  key rsa:modulus [ cert:hex "a342342bde..." ]
  	*        rsa:public_exponent [ cert:int "256" ]
  	*
  	* where the numbers are representated by blank nodes identified by their relation to a string
  	*
  	*/
  def intValueOfResourceByProperty(n: GraphNode): Option[BigInt] = {
    val hexValues = n / CERT.hex
    if (hexValues.length > 0) {
      return Some(intValueOfHexString(hexValues *))
    }
    val decimalValues = n / CERT.decimal
    if (decimalValues.length > 0) {
      return Some(BigInt(decimalValues *))
    }
    val intValues = n / CERT.int_
    if (intValues.length > 0) {
      return Some(BigInt(intValues *))
    }
    return None
  }

	/**
	 * @prefix a typed literal of some some numeric type
	 * @return the BigInteger it represents
	 */
  def intValueOfTypedLiteral(l: TypedLiteral): Option[BigInt] = {
    try {
      (l.getLexicalForm, l.getDataType) match {
        case (lf, CERT.hex) => Some(intValueOfHexString(lf))
        case (lf, CERT.decimal) => Some(BigInt(lf))
        case (lf, CERT.int_) => Some(BigInt(lf))
        case _ => Some(new BigInt(LiteralFactory.getInstance.createObject(classOf[BigInteger], l)))
      }
    } catch {
      case e: NoConvertorException => None
      case e => throw e
    }
  }



}

/**
 * An X509 Claim maintains information about the proofs associated with claims
 * found in an X509 Certificate. It is the type of object that can be passed
 * into the public credentials part of a Subject node
 *
 * todo: think of what this would look like for a chain of certificates
 *
 * @author hjs
 * @created: 30/03/2011
 */
class X509Claim(val cert: X509Certificate) extends Refreshable {

  import X509Claim._

  /* a list of unverified principals */
  lazy val webidclaims = getClaimedWebIds(cert).map {
    str => {
      val webid = new UriRef(str);
      new WebIDClaim(webid, cert.getPublicKey)
    }
  }.toSet


  //note could also implement Destroyable
  //
  //http://download.oracle.com/javase/6/docs/technotes/guides/security/jaas/JAASRefGuide.html#Credentials
  //
  //if updating validity periods can also take into account the WebID reference, then it is possible
  //that a refresh could have as consequence to do a fetch on the WebID profile
  //note: one could also take the validity period to be dependent on the validity of the profile representation
  //in which case updating the validity period would make more sense.

  override
  def refresh() {
  }

  /* The certificate is currently within the valid timzone */
  override
  def isCurrent(): Boolean = {
    val now = new Date();
    if (now.after(cert.getNotAfter())) return false
    if (now.before(cert.getNotBefore())) return false
    return true;
  }


  /**verify all the webids in the X509 */
  def verify(authService: FoafSslAuthentication) {
    webidclaims foreach {
      wid => wid.verify(authService)
    }
  }

  def canEqual(other: Any) = other.isInstanceOf[X509Claim]

  override
  def equals(other: Any): Boolean =
    other match {
      case that: X509Claim => (that eq this) || (that.canEqual(this) && cert == that.cert)
      case _ => false
    }

  override
  lazy val hashCode: Int = 41 * (41 +
    (if (cert != null) cert.hashCode else 0))


}

