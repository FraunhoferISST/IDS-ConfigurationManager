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
package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface UtilApi {
    @GetMapping(value = "/api/ui/enum/{enumName}")
    @Operation(summary = "Get the specific enum")
    @ApiResponse(responseCode = "200", description = "Successfully get the enums")
    @ApiResponse(responseCode = "400", description = "Can not find the enums")
    ResponseEntity<String> getSpecificEnum(@PathVariable String enumName);

    @PostMapping(value = "/policy-pattern")
    @Operation(summary = "Get pattern of policy")
    @ApiResponse(responseCode = "200", description = "Successfully get the pattern of policy")
    @ApiResponse(responseCode = "400", description = "Can not find the policy pattern")
    ResponseEntity<String> getPolicyPattern(@RequestBody String policy);
}
