package org.apache.clerezza.bundledevtool

import org.wymiwyg.commons.util.dirbrowser.PathNode
import java.security.AccessController
import java.security.PrivilegedAction

class PermissionGrantingPathNode(wrapped: PathNode) extends PathNode {

  def doPrivileged[T](m: => T): T = {
     AccessController.doPrivileged(new PrivilegedAction[T] {
    def run: T = {
      m
    }
  })
  }
  
  def exists(): Boolean = doPrivileged(wrapped.exists())
  def getLastModified(): java.util.Date = doPrivileged(wrapped.getLastModified())
  def getPath(): String = doPrivileged(wrapped.getPath())
  def getLength(): Long = doPrivileged(wrapped.getLength())
  def getInputStream(): java.io.InputStream = doPrivileged(wrapped.getInputStream())
  def list(): Array[String] = doPrivileged(wrapped.list())
  def list(filter: org.wymiwyg.commons.util.dirbrowser.PathNameFilter): Array[String] = doPrivileged(wrapped.list(filter))
  def isDirectory(): Boolean = doPrivileged(wrapped.isDirectory())
  def getSubPath(subPath: String): PathNode = doPrivileged(new PermissionGrantingPathNode(wrapped.getSubPath(subPath)))
}