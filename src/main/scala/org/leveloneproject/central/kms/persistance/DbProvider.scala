package org.leveloneproject.central.kms.persistance

trait DbProvider {
  this: DbProfile ⇒

  import profile.api._

  val db: Database
}
