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

package uk.gov.hmrc.mobileselfassessment.config

import com.google.inject.name.Names.named
import com.google.inject.{AbstractModule, TypeLiteral}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{CoreGet, CorePost, HttpClient}
import uk.gov.hmrc.mobileselfassessment.controllers.api.ApiAccess
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.collection.JavaConverters._

class GuiceModule(
  environment:   Environment,
  configuration: Configuration)
    extends AbstractModule {

  val servicesConfig = new ServicesConfig(
    configuration
  )

  override def configure(): Unit = {

    bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector])
    bind(classOf[CorePost]).to(classOf[WSHttpImpl])
    bindConfigInt("controllers.confidenceLevel")
    bind(classOf[ApiAccess]).toInstance(
      ApiAccess(
        "PRIVATE",
        configuration.underlying
          .getStringList("api.access.white-list.applicationIds")
          .asScala
      )
    )
  }

  private def bindConfigStringSeq(path: String): Unit = {
    val configValue: Seq[String] = configuration
      .getOptional[Seq[String]](path)
      .getOrElse(
        throw new RuntimeException(s"""Config property "$path" missing""")
      )
    bind(new TypeLiteral[Seq[String]] {})
      .annotatedWith(named(path))
      .toInstance(configValue)
  }

  private def bindConfigInt(path: String): Unit =
    bindConstant()
      .annotatedWith(named(path))
      .to(configuration.underlying.getInt(path))

  private def bindConfigString(
    name: String,
    path: String
  ): Unit =
    bindConstant().annotatedWith(named(name)).to(configuration.underlying.getString(path))
}
