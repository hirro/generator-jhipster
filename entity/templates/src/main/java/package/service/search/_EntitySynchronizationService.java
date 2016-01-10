package <%=packageName%>.service.search;

import com.mycompany.myapp.domain.<%= entityClass %>;
import com.mycompany.myapp.repository.<%= entityClass %>Repository;
import com.mycompany.myapp.repository.search.<%= entityClass %>SearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for synchronizing data between database and elasticsearch.
 */
@Service
public class <%= entityClass %>SynchronizationService
{
    <% var viaService = service != 'no';
    var instanceType = (dto == 'mapstruct') ? entityClass + 'DTO' : entityClass;
    var instanceName = (dto == 'mapstruct') ? entityInstance + 'DTO' : entityInstance; -%>

    private final Logger log = LoggerFactory.getLogger(<%= entityClass %>SynchronizationService.class);

    private boolean startupSynchronizationStarted = false;

    @Inject
    private <%= entityClass %>Repository <%= entityInstance %>Repository;   

    @Inject
    private <%= entityClass %>SearchRepository <%= entityInstance %>SearchRepository;    

    @Scheduled(initialDelayString = "${jhipster.searchSynchronizer.initialDelay:60000}", fixedRateString = "${jhipster.searchSynchronizer.fixedRate:3600000}")
    void startupSynchronization() {
        if (!startupSynchronizationStarted) {
            startupSynchronizationStarted = true;
            log.info("Startup synchronization of <%= entityClass %>");
            updateSearchRepository();
        }
    }

    @Scheduled(cron = "${jhipster.searchSynchronizer.cron:0 1 1 * * ?}")
    void scheduledSynchronization() {        
        log.info("Scheduled synchronization of <%= entityClass %>");
        updateSearchRepository();
    }

    synchronized void updateSearchRepository() {
        int addedItems = 0;
        int updateItems = 0;
        int deletedItems = 0;
        long startTime = System.currentTimeMillis();

        // Add or update subscribers in search repository
        List<<%= entityClass %>> entityList = <%= entityInstance %>Repository.findAll();
        for (<%= entityClass %> entity : entityList) {
            if (<%= entityInstance %>SearchRepository.exists(entity.getId())) {
                updateItems++;
            } else {
                addedItems++;
            }
            <%= entityInstance %>SearchRepository.save(entity);
        }

        // Find unreferenced subscribers in the search repository
        List<Long> deleteList = new ArrayList<>();
        Iterable<<%= entityClass %>> entityIter = <%= entityInstance %>SearchRepository.findAll();
        for (<%= entityClass %> entity : entityIter) {
            if (!<%= entityInstance %>Repository.exists(entity.getId())) {
                deleteList.add(entity.getId());
            }
        }

        // Delete the unreferenced items
        for (Long id : deleteList) {
            <%= entityInstance %>SearchRepository.delete(id);
        }

        log.info("Completed synchronization of elasticsearch entities for class <%= entityClass %> (added: [{}], updated: [{}], deleted: [{}], time: [{}] ms",
            addedItems, updateItems, deletedItems, System.currentTimeMillis() - startTime);
    }
}