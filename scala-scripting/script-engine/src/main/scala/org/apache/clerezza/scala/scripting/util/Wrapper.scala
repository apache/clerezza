/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.clerezza.scala.scripting.util

trait Wrapper[T] {
	protected def wrapped: T
	protected def childWrapper: (T) => T
}
