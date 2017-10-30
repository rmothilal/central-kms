package org.leveloneproject.central.kms.persistance

import java.util.UUID

import org.leveloneproject.central.kms.domain.keys.Key
import slick.lifted.ProvenShape

trait KeysTable {
  this: DbProfile ⇒

  import profile.api._

  class KeysTable(tag: Tag) extends Table[Key](tag, "Keys") {
    def id: Rep[UUID] = column[UUID]("id")
    def publicKey: Rep[String] = column[String]("public_key")
    def * : ProvenShape[Key] = (id, publicKey) <> (Key.tupled, Key.unapply)
  }

  val keys: TableQuery[KeysTable] = TableQuery[KeysTable]
}



