package org.leveloneproject.central.kms.persistance

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.batches.Batch
import slick.lifted.ProvenShape

trait BatchesTable extends DataMappers {
  this: DbProfile ⇒

  import profile.api._

  class BatchesTable(tag: Tag) extends Table[Batch](tag, "batches") {
    def id: Rep[UUID] = column[UUID]("id")
    def sidecarId: Rep[UUID] = column[UUID]("sidecar_id")
    def signature: Rep[String] = column[String]("signature")
    def timestamp: Rep[Instant] = column[Instant]("timestamp")
    def * : ProvenShape[Batch] = (id, sidecarId, signature, timestamp) <> (Batch.tupled, Batch.unapply)
  }

  val batches: TableQuery[BatchesTable] = TableQuery[BatchesTable]
}
