/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.mobileselfassessment.model

import play.api.libs.json.{Format, JsResult, JsString, JsSuccess, JsValue}
import uk.gov.hmrc.time.TaxYear.current

sealed trait DescriptionCode {
  def text(partnershipRef: Option[SaUtr]): String
}

object DescriptionCode {

  val startYear:  Int = current.currentYear
  val finishYear: Int = current.finishYear

  implicit val format: Format[DescriptionCode] = new Format[DescriptionCode] {

    override def writes(status: DescriptionCode): JsValue = status match {
      case ACI          => JsString("ACI")
      case ASST         => JsString("ASST")
      case BCD          => JsString("BCD")
      case DET          => JsString("DET")
      case DP1          => JsString("DP1")
      case DPP          => JsString("DPP")
      case ETA          => JsString("ETA")
      case IN1          => JsString("IN1")
      case IN2          => JsString("IN2")
      case JEP          => JsString("JEP")
      case LFI1         => JsString("LFI1")
      case LFI2         => JsString("LFI2")
      case LFP1         => JsString("LFP1")
      case LFP2         => JsString("LFP2")
      case LPP1         => JsString("LPP1")
      case LPP2         => JsString("LPP2")
      case LPP3         => JsString("LPP3")
      case MRMP         => JsString("MRMP")
      case NUP          => JsString("NUP")
      case OREP         => JsString("OREP")
      case PF1          => JsString("PF1")
      case PF2          => JsString("PF2")
      case PP1          => JsString("PP1")
      case PP2          => JsString("PP2")
      case RAM          => JsString("RAM")
      case REV          => JsString("REV")
      case SUP          => JsString("SUP")
      case SUR1         => JsString("SUR1")
      case SUR2         => JsString("SUR2")
      case UNKNOWN_CODE => JsString("UNKNOWN_CODE")
    }

    override def reads(json: JsValue): JsResult[DescriptionCode] = json.as[String] match {
      case "ACI"  => JsSuccess(ACI)
      case "ASST" => JsSuccess(ASST)
      case "BCD"  => JsSuccess(BCD)
      case "DET"  => JsSuccess(DET)
      case "DP1"  => JsSuccess(DP1)
      case "DPP"  => JsSuccess(DPP)
      case "ETA"  => JsSuccess(ETA)
      case "IN1"  => JsSuccess(IN1)
      case "IN2"  => JsSuccess(IN2)
      case "JEP"  => JsSuccess(JEP)
      case "LFI1" => JsSuccess(LFI1)
      case "LFI2" => JsSuccess(LFI2)
      case "LFP1" => JsSuccess(LFP1)
      case "LFP2" => JsSuccess(LFP2)
      case "LPP1" => JsSuccess(LPP1)
      case "LPP2" => JsSuccess(LPP2)
      case "LPP3" => JsSuccess(LPP3)
      case "MRMP" => JsSuccess(MRMP)
      case "NUP"  => JsSuccess(NUP)
      case "OREP" => JsSuccess(OREP)
      case "PF1"  => JsSuccess(PF1)
      case "PF2"  => JsSuccess(PF2)
      case "PP1"  => JsSuccess(PP1)
      case "PP2"  => JsSuccess(PP2)
      case "RAM"  => JsSuccess(RAM)
      case "REV"  => JsSuccess(REV)
      case "SUP"  => JsSuccess(SUP)
      case "SUR1" => JsSuccess(SUR1)
      case "SUR2" => JsSuccess(SUR2)
      case _      => JsSuccess(UNKNOWN_CODE)
    }
  }
}

case object ACI extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None): String = "Interest on late payment"
}

case object ASST extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Revenue assessment"
}

case object BCD extends DescriptionCode {

  def text(partnershipRef: Option[SaUtr] = None) =
    s"Balancing payment for ${current.currentYear} to ${current.finishYear}"
}

case object DET extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Revenue determination (estimated tax due)"
}

case object DP1 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Daily penalty"
}

case object DPP extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None): String = s"Partnership daily penalty ${partnershipRef.getOrElse("")}"
}

case object ETA extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Amendment following an enquiry"
}

case object IN1 extends DescriptionCode {

  def text(partnershipRef: Option[SaUtr] = None) =
    s"First payment on account for ${current.currentYear} to ${current.finishYear}"
}

case object IN2 extends DescriptionCode {

  def text(partnershipRef: Option[SaUtr] = None) =
    s"Second payment on account for ${current.currentYear} to ${current.finishYear}"
}

case object JEP extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Amendment following an enquiry"
}

case object LFI1 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "6 month penalty for late tax return"
}

case object LFI2 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "12 month penalty for late tax return"
}

case object LFP1 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = s"6 month penalty for late partnership tax return ${partnershipRef.getOrElse("")}"
}

case object LFP2 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = s"12 month penalty for late partnership tax return ${partnershipRef.getOrElse("")}"
}

case object LPP1 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "30 days late payment penalty"
}

case object LPP2 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "6 months late payment penalty"
}

case object LPP3 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "12 months late payment penalty"
}

case object MRMP extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Penalty"
}

case object NUP extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Amount no longer included in PAYE tax code"
}

case object OREP extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Over repayment from tax return"
}

case object PF1 extends DescriptionCode {

  def text(partnershipRef: Option[SaUtr] = None) =
    s"Penalty for late ${current.currentYear} to ${current.finishYear} tax return"
}

case object PF2 extends DescriptionCode {

  def text(partnershipRef: Option[SaUtr] = None) =
    s"Second penalty for late ${current.currentYear} to ${current.finishYear} tax return"
}

case object PP1 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = s"Penalty for late partnership tax return ${partnershipRef.getOrElse("")}"
}

case object PP2 extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = s"Second penalty for late partnership tax return ${partnershipRef.getOrElse("")}"
}

case object RAM extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Tax return amendment"
}

case object REV extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Amendment following an enquiry"
}

case object SUP extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = "Amount no longer included in PAYE tax code"
}

case object SUR1 extends DescriptionCode {

  def text(partnershipRef: Option[SaUtr] = None) =
    s"Penalty for late payment of ${current.currentYear} to ${current.finishYear} tax bill"
}

case object SUR2 extends DescriptionCode {

  def text(partnershipRef: Option[SaUtr] = None) =
    s"Second penalty for late payment of ${current.currentYear} to ${current.finishYear} tax bill"
}

case object UNKNOWN_CODE extends DescriptionCode {
  def text(partnershipRef: Option[SaUtr] = None) = s"Tax bill for ${current.currentYear} to ${current.finishYear}"
}
