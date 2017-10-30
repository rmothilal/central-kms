package org.leveloneproject.central.kms.domain.inquiries

sealed abstract class InquiryStatus(val id: Int, val value: String)

object InquiryStatus {
  def apply(id: Int): InquiryStatus = id match {
    case 1 ⇒ Created
    case 2 ⇒ Pending
    case 3 ⇒ Complete
  }

  case object Created extends InquiryStatus(1, "created")
  case object Pending extends InquiryStatus(2, "pending")
  case object Complete extends InquiryStatus(3, "complete")
}
