package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeEXIST_UNTIL.nodeEXIST_UNTIL;

@AllArgsConstructor
public class NodePOS implements StateFormula {

    public static NodePOS nodePOS(StateFormula parameter){
        return new NodePOS(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return nodeEXIST_UNTIL(TT(), parameter).evaluate();
    }

    @Override
    public String symbol() {
        return "POS";
    }

}