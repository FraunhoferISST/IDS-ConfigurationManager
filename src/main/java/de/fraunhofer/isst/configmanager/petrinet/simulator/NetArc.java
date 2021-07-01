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
package de.fraunhofer.isst.configmanager.petrinet.simulator;

import de.fraunhofer.isst.configmanager.petrinet.model.PetriNet;
import lombok.Getter;

import java.net.URI;

/**
 * Arc connecting Steps in a PetriNet execution inside the {@link StepGraph}.
 */
@Getter
public class NetArc {
    /**
     * PetriNet from which target is reachable, using a transition.
     */
    private PetriNet source;


    /**
     * PetriNet that can be reached from source, using a transition.
     */
    private PetriNet target;

    private URI usedTransition;

    public NetArc(final PetriNet source, final PetriNet target, final URI usedTransition) {
        this.source = source;
        this.target = target;
        this.usedTransition = usedTransition;
    }
}
