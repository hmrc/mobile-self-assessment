package uk.gov.hmrc.mobileselfassessment.model

import play.api.libs.json.{Format, Json}

sealed trait TaxToPayStatus

object TaxToPayStatus {
  implicit val format: Format[TaxToPayStatus] = Json.format[TaxToPayStatus]
}

case object OverDue extends TaxToPayStatus
case object CreditAndBillSame extends TaxToPayStatus
case object CreditLessThanBill extends TaxToPayStatus
case object CreditMoreThanBill extends TaxToPayStatus
case object OnlyCredit extends TaxToPayStatus
case object OnlyBill extends TaxToPayStatus
case object NoTaxToPay extends TaxToPayStatus
