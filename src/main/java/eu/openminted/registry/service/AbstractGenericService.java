package eu.openminted.registry.service;

import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Occurencies;
import eu.openminted.registry.core.domain.Paging;
import eu.openminted.registry.core.domain.Resource;
import eu.openminted.registry.core.service.ResourceService;
import eu.openminted.registry.core.service.SearchService;
import eu.openminted.registry.core.service.ServiceException;
import eu.openminted.registry.domain.*;
import eu.openminted.registry.generate.MetadataHeaderInfoGenerate;
import eu.openminted.registry.parser.ParserPool;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by stefanos on 20/6/2017.
 */

@Service("genericService")
abstract public class AbstractGenericService<T extends BaseMetadataRecord> implements ResourceCRUDService<T>{

    private Logger logger = Logger.getLogger(ComponentServiceImpl.class);

    @Autowired
    SearchService searchService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    ParserPool parserPool;

    public abstract String getResourceType();

    public abstract List<String> getFacets();

    final private Class<T> typeParameterClass;

    public AbstractGenericService(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    @Override
    public T get(String id) {
        T resource;
        try {
            resource = Utils.serialize(searchService.searchId(getResourceType(), id), typeParameterClass);
        } catch (UnknownHostException e) {
            logger.fatal(e);
            throw new ServiceException(e);
        }
        return resource;
    }

    @Override
    public Browsing getAll(FacetFilter filter) {
        filter.addFilter("public",true);

        filter.setBrowseBy(getFacets());

        return getResults(filter);
    }

    @Override
    public Browsing getMy(FacetFilter filter) {
        OIDCAuthenticationToken authentication = (OIDCAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        filter.addFilter("personIdentifier",authentication.getSub());
        return getResults(filter);
    }

    @Override
    public void add(T resource) {
        if(resource.getMetadataHeaderInfo() == null) {
            logger.info("Auto-generate metadata header info for " + getResourceType());
            resource.setMetadataHeaderInfo(MetadataHeaderInfoGenerate.generate());
        }

        String insertionId = resource.getMetadataHeaderInfo().getMetadataRecordIdentifier().getValue();
        Resource checkResource;

        try {
            //Check existence if resource
            checkResource = searchService.searchId(getResourceType(), insertionId);
        } catch (UnknownHostException e ) {
            logger.fatal(e);
            throw new ServiceException(e);
        }

        if (checkResource != null) {
            throw new ServiceException(String.format("%s with id [%s] already exists",getResourceType(),insertionId));
        }



        Resource resourceDb = new Resource();


        Future<String> serialized = parserPool.unserialize(resource, typeParameterClass);
        try {
            resourceDb.setCreationDate(new Date());
            resourceDb.setModificationDate(new Date());
            resourceDb.setPayloadFormat("xml");
            resourceDb.setResourceType(getResourceType());
            resourceDb.setVersion("not_set");
            resourceDb.setId(insertionId);
            resourceDb.setPayload(serialized.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException(e);
        }
        resourceService.addResource(resourceDb);
    }

    @Override
    public void update(T resources) {
        Resource $resource;

        try {
            $resource = searchService.searchId(getResourceType(), resources.getMetadataHeaderInfo().getMetadataRecordIdentifier().getValue());
        } catch (UnknownHostException e) {
            logger.fatal(e);
            throw new ServiceException(e);
        }

        Resource resource = new Resource();

        if ($resource == null) {
            throw new ServiceException(getResourceType() + " does not exists");
        } else {
            String serialized = Utils.unserialize(resources, typeParameterClass);

            if (!serialized.equals("failed")) {
                resource.setPayload(serialized);
            } else {
                throw new ServiceException("Serialization failed");
            }
            resource = $resource;
            resource.setPayloadFormat("xml");
            resource.setPayload(serialized);
            resourceService.updateResource(resource);
        }
    }

    @Override
    public void delete(T component) {
        Resource resource;
        try {
            resource = searchService.searchId(getResourceType(), component.getMetadataHeaderInfo().getMetadataRecordIdentifier().getValue());
            if (resource == null) {
                throw new ServiceException(getResourceType() + " does not exists");
            } else {
                resourceService.deleteResource(resource.getId());
            }
        } catch (UnknownHostException e) {
            logger.fatal(e);
            throw new ServiceException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Browsing<T> getResults(FacetFilter filter) {
        List<Order<T>> result = new ArrayList<>();
        List<Future<T>> futureResults;
        Paging paging;
        filter.setResourceType(getResourceType());
        Browsing<T> browsing;
        Occurencies overall;
        List<Facet> facetsCollection;
        try {
            paging = searchService.search(filter);
            futureResults = new ArrayList<>(paging.getResults().size());
            int index = 0;
            for(Object res : paging.getResults()) {
                Resource resource = (Resource) res;
                futureResults.add(index,parserPool.serialize(resource,typeParameterClass));
                index++;
            }
            overall = paging.getOccurencies();
            facetsCollection = RequestServiceImpl.createFacetCollection(overall);
            for(Future<T> res : futureResults) {
                result.add(new Order(index,res.get()));
            }
        } catch (UnknownHostException | InterruptedException | ExecutionException e ) {
            logger.fatal(e);
            throw new ServiceException(e);
        }
        browsing = new Browsing(paging.getTotal(), filter.getFrom(), filter.getFrom() + result.size(), result, facetsCollection);
        return browsing;
    }
}
