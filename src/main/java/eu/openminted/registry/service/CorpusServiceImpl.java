package eu.openminted.registry.service;

import eu.openminted.registry.core.domain.Resource;
import eu.openminted.registry.core.service.ResourceService;
import eu.openminted.registry.core.service.SearchService;
import eu.openminted.registry.core.service.ServiceException;
import eu.openminted.registry.domain.Component;
import eu.openminted.registry.domain.Corpus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.Date;

/**
 * Created by stefanos on 15-Nov-16.
 */
@Service("corpusService")
public class CorpusServiceImpl implements ResourceCRUDService<Corpus> {

    private Logger logger = Logger.getLogger(CorpusServiceImpl.class);

    @Autowired
    SearchService searchService;

    @Autowired
    ResourceService resourceService;

    @Override
    public Corpus get(String id) {
        Corpus resource;
        try {
            resource = Utils.serialize(searchService.searchId("corpus", id), Corpus.class);
        } catch (UnknownHostException e) {
            logger.fatal(e);
            throw new ServiceException(e);
        }
        return resource;
    }

    @Override
    public void add(Corpus corpus) {
        Corpus $corpus;
        try {
            $corpus = Utils.serialize(searchService.searchId("corpus",
                    corpus.getMetadataHeaderInfo().getMetadataRecordIdentifier().getValue()), Corpus.class);
        } catch (UnknownHostException e) {
            logger.fatal(e);
            throw new ServiceException(e);
        }

        if ($corpus != null) {
            throw new ServiceException("Component already exists");
        }

        Resource resource = new Resource();

        String serialized = Utils.unserialize(corpus, Corpus.class);

        if (!serialized.equals("failed")) {
            resource.setPayload(serialized);
        } else {
            throw new ServiceException("Serialization failed");
        }

        resource.setCreationDate(new Date());
        resource.setModificationDate(new Date());
        resource.setPayloadFormat("xml");
        resource.setResourceType("component");
        resource.setVersion("not_set");
        resource.setId("wont be saved");


        resourceService.addResource(resource);
    }

    @Override
    public void update(Corpus corpus) {
        Resource $resource;
        Resource resource = new Resource();
        try {
            $resource = searchService.searchId("component", corpus.getMetadataHeaderInfo().getMetadataRecordIdentifier().getValue());
        } catch (UnknownHostException e) {
            logger.fatal(e);
            throw new ServiceException(e);
        }

        if ($resource != null) {
            throw new ServiceException("Component already exists");
        } else {
            String serialized = Utils.unserialize(corpus, Corpus.class);

            if (!serialized.equals("failed")) {
                resource.setPayload(serialized);
            } else {
                throw new ServiceException("Serialization failed");
            }
            resource = (Resource) $resource;
            resource.setPayloadFormat("xml");
            resource.setPayload(serialized);
            resourceService.updateResource(resource);
        }
    }

    @Override
    public void delete(Corpus corpus) {
        Resource resource;
        try {
            resource = searchService.searchId("corpus", corpus.getMetadataHeaderInfo().getMetadataRecordIdentifier().getValue());
            if (resource != null) {
                throw new ServiceException("Component already exists");
            } else {
                resourceService.deleteResource(resource.getId());
            }
        } catch (UnknownHostException e) {
            logger.fatal(e);
            throw new ServiceException(e);
        }
    }
}