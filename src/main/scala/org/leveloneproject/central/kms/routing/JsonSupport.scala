package org.leveloneproject.central.kms.routing

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.leveloneproject.central.kms.util.{JsonFormats, JsonSerialization}

trait JsonSupport extends Json4sSupport with JsonSerialization with JsonFormats
