/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.mobileselfassessment.config.binders

import play.api.mvc.PathBindable
import uk.gov.hmrc.mobileselfassessment.model.SaUtr

object Binders {

  implicit def utrBinder(implicit stringBinder: PathBindable[String]): PathBindable[SaUtr] = new PathBindable[SaUtr] {

    def unbind(
      key: String,
      utr: SaUtr
    ): String = stringBinder.unbind(key, utr.value)

    def bind(
      key: String,
      value: String
    ): Either[String, SaUtr] =
      Right(SaUtr(value))
  }
}
