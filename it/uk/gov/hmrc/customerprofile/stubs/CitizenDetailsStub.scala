/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.customerprofile.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.domain.Nino

object CitizenDetailsStub {

  def designatoryDetailsForNinoAre(nino: Nino, detailsJson: String): Unit =
    stubFor(get(urlEqualTo(s"/citizen-details/${nino.value}/designatory-details"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody(detailsJson)))

  def citizenDetailsErrorResponse(nino: Nino, responseStatusCode: Int): Unit =
    stubFor(get(urlEqualTo(s"/citizen-details/${nino.value}/designatory-details"))
      .willReturn(aResponse()
        .withStatus(responseStatusCode)))
}
