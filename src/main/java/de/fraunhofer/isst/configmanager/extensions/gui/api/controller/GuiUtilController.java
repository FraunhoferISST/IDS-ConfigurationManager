/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.extensions.gui.api.controller;

import de.fraunhofer.isst.configmanager.extensions.gui.api.GuiUtilApi;
import de.fraunhofer.isst.configmanager.extensions.gui.api.service.GuiUtilService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * The api class offers the possibilities to provide other api's which could be needed.
 * As an example, enum values are supplied here via an api.
 */
@Slf4j
@RestController
@Tag(name = "Extension: GUI Utilities")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GuiUtilController implements GuiUtilApi {

    private final transient GuiUtilService utilService;

    /**
     * This method returns for a given enum name all enum values.
     *
     * @param enumName name of the enum
     * @return enum values as a string
     */
    @Override
    public ResponseEntity<String> getSpecificEnum(final String enumName) {
        if (log.isInfoEnabled()) {
            log.info(">> GET /api/ui/enum " + enumName);
        }
        ResponseEntity<String> response;

        final var enums = utilService.getSpecificEnum(enumName);

        if (enums != null) {
            response = ResponseEntity.ok(enums);
        } else {
            response = ResponseEntity.badRequest().body("Could not get the enums");
        }

        return response;
    }
}
