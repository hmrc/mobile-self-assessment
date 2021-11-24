/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}

sealed trait TaxToPayStatus
case object OverdueWithBill extends TaxToPayStatus
case object Overdue extends TaxToPayStatus
case object CreditAndBillSame extends TaxToPayStatus
case object CreditLessThanBill extends TaxToPayStatus
case object CreditMoreThanBill extends TaxToPayStatus
case object OnlyCredit extends TaxToPayStatus
case object OnlyBill extends TaxToPayStatus
case object NoTaxToPay extends TaxToPayStatus

object TaxToPayStatus extends TaxToPayStatus {

  implicit val taxToPayStatusFormat: Format[TaxToPayStatus] = new Format[TaxToPayStatus] {

    override def reads(json: JsValue): JsSuccess[TaxToPayStatus] = json.as[String] match {
      case "OverdueWithBill"    => JsSuccess(OverdueWithBill)
      case "Overdue"            => JsSuccess(Overdue)
      case "CreditAndBillSame"  => JsSuccess(CreditAndBillSame)
      case "CreditLessThanBill" => JsSuccess(CreditLessThanBill)
      case "CreditMoreThanBill" => JsSuccess(CreditMoreThanBill)
      case "OnlyCredit"         => JsSuccess(OnlyCredit)
      case "OnlyBill"           => JsSuccess(OnlyBill)
      case "NoTaxToPay"         => JsSuccess(NoTaxToPay)
      case _                    => throw new IllegalArgumentException("Invalid taxToPay status value")
    }

    override def writes(taxToPayStatus: TaxToPayStatus) = JsString(taxToPayStatus.toString)
  }
}
