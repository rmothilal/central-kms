package org.leveloneproject.central.kms.util

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.{TemporalAccessor, TemporalQuery}

import org.json4s.JsonAST.JString
import org.json4s._
import org.json4s.ext.JavaTypesSerializers
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheckLevel, HealthCheckStatus}
import org.leveloneproject.central.kms.domain.inquiries.InquiryStatus
import org.leveloneproject.central.kms.domain.sidecars.SidecarStatus
import org.leveloneproject.central.kms.socket._

import scala.util.{Failure, Success, Try}

trait JsonSerialization {
  implicit val serialization = native.Serialization
}

trait JsonFormats {
  implicit val formats: Formats = DefaultFormats ++ JavaTypesSerializers.all ++ CustomSerializers.defaults
}

trait JsonSerializer extends JsonSerialization with JsonFormats {

  import org.json4s.native.Serialization.write

  def serialize[T <: AnyRef](obj: T)(implicit formats: Formats): String = {
    write(obj)
  }
}

trait JsonDeserializer extends JsonSerialization with JsonFormats {

  import serialization._

  def deserialize[T](json: String)(implicit mf: Manifest[T]): T = {
    read[T](json)
  }

  def deserializeSafe[T](json: String)(implicit mf: Manifest[T]): Either[KmsError, T] = {
    Try(deserialize[T](json)) match {
      case Success(result) ⇒ Right(result)
      case Failure(e) ⇒ e match {
        case _: MappingException ⇒ Left(KmsError.invalidRequest)
        case _ ⇒ Left(KmsError.parseError)
      }
    }
  }

  def extract[T](obj: Any)(implicit mf: Manifest[T]): T = {
    obj match {
      case s: String ⇒ deserialize[T](s)
      case j: JValue ⇒ extractType(j)
      case _ ⇒ extractType(Extraction.decompose(obj))
    }
  }

  def extractSafe[T](obj: Any)(implicit mf: Manifest[T]): Option[T] = {
    try {
      Some(extract[T](obj))
    } catch {
      case _: Throwable ⇒ None
    }
  }

  private def extractType[T](jValue: JValue)(implicit formats: Formats, mf: Manifest[T]) = {
    jValue.extract[T]
  }
}

object CustomSerializers {

  val defaults = Seq(
    InstantSerializer,
    HealthCheckLevelSerializer,
    HealthCheckStatusSerializer,
    SidecarStatusSerializer,
    InquiryStatusSerializer,
    JsonMessageSerializer
  )

  object InstantSerializer extends InstantSerializer(DateTimeFormatter.ISO_INSTANT)

  object HealthCheckLevelSerializer extends HealthCheckLevelSerializer

  object HealthCheckStatusSerializer extends HealthCheckStatusSerializer

  object SidecarStatusSerializer extends SidecarStatusSerializer

  object InquiryStatusSerializer extends InquiryStatusSerializer

  object JsonMessageSerializer extends JsonMessageSerializer

  class InstantSerializer(val format: DateTimeFormatter) extends CustomSerializer[Instant](_ ⇒ ( {
    case JString(s) => format.parse(s, asQuery(Instant.from))
  }, {
    case t: Instant ⇒ JString(format.format(t))
  }
  ))

  class HealthCheckLevelSerializer extends CustomSerializer[HealthCheckLevel](_ ⇒ ( {
    case JString(s) ⇒ s match {
      case HealthCheckLevel.Ping.value ⇒ HealthCheckLevel.Ping
      case _ ⇒ throw new MappingException("")
    }
  }, {
    case l: HealthCheckLevel ⇒ JString(l.value)
  }
  ))

  class HealthCheckStatusSerializer extends CustomSerializer[HealthCheckStatus](_ ⇒ ( {
    case JString(s) ⇒ HealthCheckStatus(s)
  }, {
    case l: HealthCheckStatus ⇒ JString(l.value)
  }
  ))

  class SidecarStatusSerializer extends CustomSerializer[SidecarStatus](_ ⇒ ( {
    case JString(s) ⇒ s match {
      case SidecarStatus.Registered.value ⇒ SidecarStatus.Registered
      case SidecarStatus.Terminated.value ⇒ SidecarStatus.Terminated
      case SidecarStatus.Challenged.value ⇒ SidecarStatus.Challenged
      case SidecarStatus.Suspended.value ⇒ SidecarStatus.Suspended
    }
  }, {
    case l: SidecarStatus ⇒ JString(l.value)
  }
  ))

  class InquiryStatusSerializer extends CustomSerializer[InquiryStatus](_ ⇒
    ( {
      case JString(s) ⇒ s match {
        case InquiryStatus.Pending.value ⇒ InquiryStatus.Pending
        case InquiryStatus.Created.value ⇒ InquiryStatus.Created
        case InquiryStatus.Complete.value ⇒ InquiryStatus.Complete
      }
    }, {
      case i: InquiryStatus ⇒ JString(i.value)
    }
    )
  )

  class JsonMessageSerializer extends Serializer[JsonMessage] {
    private val JsonMessageClass = implicitly[Manifest[JsonMessage]].runtimeClass
    implicit val formats: Formats = (DefaultFormats + HealthCheckLevelSerializer + InstantSerializer) ++ JavaTypesSerializers.all

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), JsonMessage] = deserialize()

    private def deserialize(): PartialFunction[(TypeInfo, JValue), JsonMessage] = {
      case (TypeInfo(JsonMessageClass, _), json) ⇒ json match {
        case value: JValue if value.extractOpt[JsonRequest].isDefined ⇒ value.extract[JsonRequest]
        case value: JValue if value.extractOpt[JsonResponse].isDefined ⇒ value.extract[JsonResponse]
        case value ⇒ throw new MappingException(s"Can't convert $value to $JsonMessageClass")
      }
    }

    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = serialize()

    private def serialize(): PartialFunction[Any, JValue] = {
      case value: JsonMessage ⇒ Extraction.decompose(value)
    }
  }

  def asQuery[T](f: TemporalAccessor ⇒ T): TemporalQuery[T] =
    (temporal: TemporalAccessor) => f(temporal)
}
