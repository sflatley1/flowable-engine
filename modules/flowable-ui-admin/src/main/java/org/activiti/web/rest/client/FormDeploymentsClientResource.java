/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.web.rest.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.activiti.domain.EndpointType;
import org.activiti.domain.ServerConfig;
import org.activiti.service.engine.FormDeploymentService;
import org.activiti.service.engine.exception.ActivitiServiceException;
import org.activiti.web.rest.exception.BadRequestException;
import org.activiti.web.rest.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * @author Yvo Swillens
 */
@RestController
@RequestMapping("/rest/activiti/form-deployments")
public class FormDeploymentsClientResource extends AbstractClientResource {

  private final Logger log = LoggerFactory.getLogger(FormDeploymentsClientResource.class);

  @Autowired
  protected FormDeploymentService clientService;

  /**
   * GET /rest/activiti/form-deployments -> get a list of form deployments.
   */
  @RequestMapping(method = RequestMethod.GET, produces = "application/json")
  public JsonNode listDeployments(HttpServletRequest request) {
    log.debug("REST request to get a list of form deployments");

    JsonNode resultNode = null;
    ServerConfig serverConfig = retrieveServerConfig(EndpointType.FORM);
    Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);

    try {
      resultNode = clientService.listDeployments(serverConfig, parameterMap);

    } catch (ActivitiServiceException e) {
      throw new BadRequestException(e.getMessage());
    }

    return resultNode;
  }

  /**
   * POST /rest/activiti/form-deployments: upload a form deployment
   */
  @RequestMapping(method = RequestMethod.POST, produces = "application/json")
  public JsonNode handleFileUpload(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
    if (!file.isEmpty()) {
      try {
        ServerConfig serverConfig = retrieveServerConfig(EndpointType.FORM);
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".form") || fileName.endsWith(".xml"))) {

          return clientService.uploadDeployment(serverConfig, fileName, file.getInputStream());

        } else {
          throw new BadRequestException("Invalid file name");
        }

      } catch (IOException e) {
        throw new InternalServerErrorException("Could not deploy file: " + e.getMessage());
      }

    } else {
      throw new BadRequestException("No file found in POST body");
    }
  }

}