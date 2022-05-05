package datawave.microservice;

import datawave.accumulo.util.security.UserAuthFunctions;
import datawave.microservice.authorization.user.ProxiedUserDetails;
import datawave.microservice.dictionary.config.DataDictionaryProperties;
import datawave.security.authorization.DatawaveUser;
import datawave.security.util.ScannerHelper;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Provides a connection to accumulo for use by controllers
 *
 * If other services (other than dictionary/model) are going to need to connect to accumulo, should we move this into the accumulo service and pass the
 * individual properties (instead of a properties object and calling getters)
 */
@Service
public class AccumuloConnectionService {
    
    private final Connector accumuloConnector;
    private final DataDictionaryProperties dataDictionaryConfiguration;
    private final UserAuthFunctions userAuthFunctions;

    // These were already hardcodeded values in ModelBean -> ModelController so therefore they weren't made configurable
    private static final long BATCH_WRITER_MAX_LATENCY = 1000L;
    private static final long BATCH_WRITER_MAX_MEMORY = 10845760;
    private static final int BATCH_WRITER_MAX_THREADS = 2;
    
    public AccumuloConnectionService(DataDictionaryProperties dataDictionaryConfiguration, UserAuthFunctions userAuthFunctions,
                    @Qualifier("warehouse") Connector accumuloConnector) {
        this.dataDictionaryConfiguration = dataDictionaryConfiguration;
        this.userAuthFunctions = userAuthFunctions;
        this.accumuloConnector = accumuloConnector;
    }
    
    public Connection getConnection(String modelTable, String modelName, ProxiedUserDetails user) {
        return getConnection(dataDictionaryConfiguration.getMetadataTableName(), modelTable, modelName, user);
    }
    
    public Connection getConnection(String metadataTable, String modelTable, String modelName, ProxiedUserDetails user) {
        Connection helper = new Connection();
        helper.setMetadataTable(getSupplierValueIfBlank(metadataTable, dataDictionaryConfiguration::getMetadataTableName));
        helper.setModelTable(getSupplierValueIfBlank(modelTable, dataDictionaryConfiguration::getModelTableName));
        helper.setModelName(getSupplierValueIfBlank(modelName, dataDictionaryConfiguration::getModelName));
        helper.setAuths(getAuths(user));
        helper.setConnector(accumuloConnector);
        return helper;
    }
    
    private String getSupplierValueIfBlank(final String value, final Supplier<String> supplier) {
        return StringUtils.isBlank(value) ? supplier.get() : value;
    }
    
    public Set<Authorizations> getAuths(ProxiedUserDetails currentUser) {
        //@formatter:off
        return currentUser.getProxiedUsers().stream()
                .map(DatawaveUser::getAuths)
                .map(a -> new Authorizations(a.toArray(new String[0])))
                .collect(Collectors.toSet());
        //@formatter:on
    }
    
    public Set<Authorizations> getDowngradedAuthorizations(String requestedAuthorizations, ProxiedUserDetails currentUser) {
        DatawaveUser primaryUser = currentUser.getPrimaryUser();
        return userAuthFunctions.mergeAuthorizations(userAuthFunctions.getRequestedAuthorizations(requestedAuthorizations, currentUser.getPrimaryUser()),
                        currentUser.getProxiedUsers(), u -> u != primaryUser);
    }
    
    public Scanner getScanner(String modelTable, ProxiedUserDetails currentUser) throws TableNotFoundException {
        return ScannerHelper.createScanner(this.accumuloConnector, modelTable, this.getAuths(currentUser));
    }
    
    public Scanner getScannerWithRegexIteratorSetting(String name, String modelTable, ProxiedUserDetails currentUser) throws TableNotFoundException {
        IteratorSetting cfg = new IteratorSetting(21, "colfRegex", RegExFilter.class.getName());
        cfg.addOption(RegExFilter.COLF_REGEX, "^" + name + "(\\x00.*)?");
        
        Scanner scanner = getScanner(modelTable, currentUser);
        scanner.addScanIterator(cfg);
        return scanner;
    }

    public BatchWriter getDefaultBatchWriter(String modelTable, String modelName, ProxiedUserDetails user) throws TableNotFoundException {
        Connector connector = getConnection(modelTable, modelName, user).getConnector();
        // TODO Do we need a new instance of BatchWriterConfig each time, or can this be a static or bean object?
        return connector.createBatchWriter(modelTable, new BatchWriterConfig().setMaxLatency(BATCH_WRITER_MAX_LATENCY, TimeUnit.MILLISECONDS).setMaxMemory(BATCH_WRITER_MAX_MEMORY)
                .setMaxWriteThreads(BATCH_WRITER_MAX_THREADS));
    }
}
