package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.organisation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ECountryCode;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignedBuc;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.OrgContactMethod;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.OrganisationFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToOrganisationMapper extends AbstractMapToEntityMapper<MapHolder, Organisation> {

    @Override
    public void mapAtoB(MapHolder a, Organisation b, MappingContext mappingContext) {

        b.setId(a.string(OrganisationFields.ID));
        b.setAcronym(a.string(OrganisationFields.ACRONYM));
        b.setRegistryNumber(a.string(OrganisationFields.REGISTRY_NUMBER));
        b.setName(a.string(OrganisationFields.NAME));
        b.setCountryCode(ECountryCode.valueOf(a.string(OrganisationFields.COUNTRY_CODE)));

        mapDate(a, OrganisationFields.ACTIVE_SINCE, b::setActiveSince);

        b.setLocation(a.string(OrganisationFields.LOCATION));

        mapAddress(b, a);
        mapContactMethods(b, mappingContext, a);
        mapAccessPoint(b, a);
        mapAssignedBucs(b, mappingContext, a);
        mapLog(a, b);
    }

    private void mapLog(final MapHolder a, final Organisation b) {
        setDefaultLog(b::setLog);
    }

    private void mapAssignedBucs(Organisation b, MappingContext mappingContext, MapHolder a) {
        b.getAssignedBucs();
        List<MapHolder> assignedBucs = a.listToMapHolder(OrganisationFields.ASSIGNED_BUCS);

        if (!CollectionUtils.isEmpty(assignedBucs)) {
            assignedBucs.stream()
                    .map(mapHolder -> mapperFacade.map(mapHolder, AssignedBuc.class, mappingContext))
                    .forEach(b::addAssignedBuc);
        }
    }

    private void mapAccessPoint(Organisation b, MapHolder a) {
        MapHolder ap = a.getMapHolder(OrganisationFields.ACCESS_POINT);

        b.setApId(ap.string(OrganisationFields.ACCESS_POINT_ID));
        b.setApChannel(ap.string(OrganisationFields.ACCESS_POINT_CHANNEL));
        b.setApCountryCode(ECountryCode.valueOf(ap.string(OrganisationFields.ACCESS_POINT_COUNTRY_CODE)));
        b.setApInboxService(ap.string(OrganisationFields.ACCESS_POINT_INBOX_SERVICE));
        b.setApIp(ap.string(OrganisationFields.ACCESS_POINT_IP));
        b.setApName(ap.string(OrganisationFields.ACCESS_POINT_NAME));
        b.setApOutboxService(ap.string(OrganisationFields.ACCESS_POINT_OUTBOX_SERVICE));
        b.setApPort(Integer.valueOf(ap.string(OrganisationFields.ACCESS_POINT_PORT)));
        b.setApProtocol(ap.string(OrganisationFields.ACCESS_POINT_PROTOCOL));
        b.setApTechnicalChannel(ap.string(OrganisationFields.ACCESS_POINT_TECHNICAL_CHANNEL));
        b.setApTechnicalInboxService(ap.string(OrganisationFields.ACCESS_POINT_TECHNICAL_INBOX_SERVICE));
        b.setApTechnicalOutboxService(ap.string(OrganisationFields.ACCESS_POINT_TECHNICAL_OUTBOX_SERVICE));
        b.setApTechnicalProtocol(ap.string(OrganisationFields.ACCESS_POINT_TECHNICAL_PROTOCOL));
    }

    private void mapContactMethods(Organisation b, MappingContext mappingContext, MapHolder a) {
        b.getOrgContactMethods();
        List<MapHolder> contactMethods = a.listToMapHolder(OrganisationFields.CONTACT_METHODS);
        if (!CollectionUtils.isEmpty(contactMethods)) {

            List<OrgContactMethod> orgContactMethods = contactMethods.stream()
                    .map(mapHolder -> mapperFacade.map(mapHolder, OrgContactMethod.class, mappingContext))
                    .peek(orgContactMethod -> orgContactMethod.setOrganisation(b))
                    .collect(Collectors.toList());

            b.setOrgContactMethods(orgContactMethods);
        }
    }

    private void mapAddress(Organisation b, MapHolder a) {
        MapHolder address = a.getMapHolder(OrganisationFields.ADDRESS);

        b.setAddressStreet(address.string(OrganisationFields.ADDRESS_STREET));
        b.setAddressTown(address.string(OrganisationFields.ADDRESS_TOWN));
        b.setAddressPostalCode(address.string(OrganisationFields.ADDRESS_POSTAL_CODE));
        b.setAddressRegion(address.string(OrganisationFields.ADDRESS_REGION));
        b.setAddressCountry(address.string(OrganisationFields.ADDRESS_COUNTRY));
    }

}
