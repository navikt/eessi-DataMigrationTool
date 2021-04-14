package eu.ec.dgempl.eessi.rina.tool.migration.buc.receiveaction;

import java.util.Optional;

import eu.ec.dgempl.eessi.rina.buc.core.model.Case;
import eu.ec.dgempl.eessi.rina.buc.core.model.CaseParameters;
import eu.ec.dgempl.eessi.rina.buc.core.model.ECaseParameterType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;

/**
 * Interface for a producer of receive actions. The implementations of this class are handling creation of receive actions for various
 * scenarios.
 */
public interface ReceiveActionProducer {

    void createReceiveActions(RinaCase rinaCase, Case bucDefinition) throws Exception;

    default boolean isMLC(Case bucDefinition) {
        Optional<CaseParameters.Parameter> parameter = bucDefinition.getContext().getParameters().getParameter()
                .stream()
                .filter(param -> ECaseParameterType.IS_ML == param.getKey())
                .findFirst();

        return parameter.map(value -> Boolean.parseBoolean(value.getValue())).orElse(Boolean.FALSE);
    }

}
